import express from "express";
import dotenv from "dotenv";
import cors from "cors";
import cookieParser from "cookie-parser";
import { connectDB } from "./config/db.js";
import { errorHandlerMiddleware } from "./middlewares/errorHandler.js";
import authRoute from "./routes/authRoute.js";
import userRoute from "./routes/userRoute.js";
import adminRoute from "./routes/adminRoute.js";
import vendorRoute from "./routes/venderRoute.js";
import { cloudinaryConfig } from "./utils/cloudinaryConfig.js";

dotenv.config();

const App = express();
const port = process.env.PORT || 3000;

connectDB();

App.use(express.json());
App.use(cookieParser());

const allowedOrigins = [
  "http://localhost:5173",
];
App.use(
  cors({
    origin: allowedOrigins,
    methods: ["GET", "PUT", "POST", "PATCH", "DELETE"],
    credentials: true,
  })
);

App.use("*", cloudinaryConfig);

App.use("/api/auth", authRoute);
App.use("/api/user", userRoute);
App.use("/api/admin", adminRoute);
App.use("/api/vendor", vendorRoute);

App.use(errorHandlerMiddleware);

App.listen(port, () => {
  console.log(`Server listening on port ${port}!`);
});
