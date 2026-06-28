import jwt from "jsonwebtoken";
import { errorHandler } from "../utils/error.js";
import User from "../models/userModel.js";

export const verifyToken = async (req, res, next) => {
  if (!req.headers.authorization) {
    return next(errorHandler(403, "bad request no header provided"));
  }

  const authHeader = req.headers.authorization;
  if (!authHeader.startsWith("Bearer ")) {
    return next(errorHandler(403, "Invalid authorization format"));
  }

  const tokenParts = authHeader.split(" ")[1];
  if (!tokenParts) {
    return next(errorHandler(401, "You are not authenticated"));
  }

  const refreshToken = tokenParts.split(",")[0];
  const accessToken = tokenParts.split(",")[1];

  if (!accessToken) {
    if (!refreshToken) {
      return next(errorHandler(401, "You are not authenticated"));
    }

    try {
      const decoded = jwt.verify(refreshToken, process.env.REFRESH_TOKEN);
      const user = await User.findById(decoded.id);

      if (!user) return next(errorHandler(403, "Invalid refresh token"));

      if (user.refreshToken !== refreshToken)
        return next(errorHandler(403, "Invalid refresh token"));

      const newAccessToken = jwt.sign(
        { id: user._id },
        process.env.ACCESS_TOKEN,
        { expiresIn: "15m" }
      );
      const newRefreshToken = jwt.sign(
        { id: user._id },
        process.env.REFRESH_TOKEN,
        { expiresIn: "7d" }
      );

      // Update the refresh token in the database for the user
      await User.updateOne(
        { _id: user._id },
        { refreshToken: newRefreshToken }
      );

      req.user = decoded.id; // setting req.user so that next middleware in this cycle can access it
      next();
    } catch (error) {
      console.error("Refresh token verification error:", error);
      next(error);
    }
  } else {
    try {
      const decoded = jwt.verify(accessToken, process.env.ACCESS_TOKEN);
      req.user = decoded.id; // setting req.user so that next middleware in this cycle can access it
      next();
    } catch (error) {
      if (error.name === "TokenExpiredError") {
        if (!refreshToken) {
          return next(errorHandler(401, "You are not authenticated"));
        }

        // Access token expired, try to refresh it using the refresh token
        try {
          const decodedRefresh = jwt.verify(refreshToken, process.env.REFRESH_TOKEN);
          const user = await User.findById(decodedRefresh.id);

          if (!user || user.refreshToken !== refreshToken) {
            return next(errorHandler(403, "Invalid refresh token"));
          }

          const newAccessToken = jwt.sign(
            { id: user._id },
            process.env.ACCESS_TOKEN,
            { expiresIn: "15m" }
          );
          const newRefreshToken = jwt.sign(
            { id: user._id },
            process.env.REFRESH_TOKEN,
            { expiresIn: "7d" }
          );

          await User.updateOne(
            { _id: user._id },
            { refreshToken: newRefreshToken }
          );

          req.user = decodedRefresh.id;
          next();
        } catch (refreshErr) {
          console.error("Error refreshing token after access token expiry:", refreshErr);
          next(errorHandler(403, "Token is not valid"));
        }
      } else {
        next(errorHandler(403, "Token is not valid"));
      }
    }
  }
};
