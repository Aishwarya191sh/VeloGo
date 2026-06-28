import express from "express";
import dotenv from "dotenv";
import cors from "cors";
import cookieParser from "cookie-parser";
import { connectDB } from "./config/db.js";
import { errorHandlerMiddleware } from "./middlewares/errorHandler.js";
import authRoute from "./routes/authRoute.js";

dotenv.config();

const App = express();
const port = process.env.PORT || 3000;

connectDB();

App.use(express.json());
App.use(cookieParser());

const allowedOrigins = [
  "https://rent-a-ride-two.vercel.app",
  "http://localhost:5173",
];
App.use(
  cors({
    origin: allowedOrigins,
    methods: ["GET", "PUT", "POST", "PATCH", "DELETE"],
    credentials: true,
  })
);

App.use("/api/auth", authRoute);

App.use(errorHandlerMiddleware);

App.listen(port, () => {
  console.log(`Server listening on port ${port}!`);
});
