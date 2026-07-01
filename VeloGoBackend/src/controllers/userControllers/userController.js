import { errorHandler } from "../../utils/error.js";

//signOut 
export const signOut = async (req, res, next) => {
  try {
    res.status(200).json({ message: "signedOut successfully" });
  } catch (error) {
    console.log(error);
    next(errorHandler(500, "error in signout controller"));
  }
};
