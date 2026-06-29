import express from "express";
import { listAllVehicles, showVehicleDetails } from "../controllers/userControllers/userAllVehiclesController.js";
import {
  getVehiclesWithoutBooking,
  filterVehicles,
  showOneofkind,
  showAllVariants,
  razorpayOrder,
  BookCar,
  latestbookings,
} from "../controllers/userControllers/userBookingController.js";

const router = express.Router();

router.get("/listAllVehicles", listAllVehicles);
router.post("/showVehicleDetails", showVehicleDetails);
router.post("/filterVehicles", filterVehicles);
router.post("/getVehiclesWithoutBooking", getVehiclesWithoutBooking, showAllVariants);
router.post("/showSingleofSameModel", getVehiclesWithoutBooking, showOneofkind);

// Payment & booking routes
router.post("/razorpay", razorpayOrder);
router.post("/bookCar", BookCar);
router.post("/latestbookings", latestbookings);

export default router;
