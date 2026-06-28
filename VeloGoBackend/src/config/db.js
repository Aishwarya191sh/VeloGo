import mongoose from "mongoose";

export const connectDB = async () => {
  try {
    const mongoUri = process.env.mongo_uri;
    if (!mongoUri) {
      throw new Error("mongo_uri is not defined inside the environment configuration.");
    }
    const conn = await mongoose.connect(mongoUri);
    console.log(`Database connected successfully: ${conn.connection.host}`);
  } catch (error) {
    console.error(`Database connection failed: ${error.message}`);
    process.exit(1);
  }
};
