import express from "express";
import { listAllVehicles, showVehicleDetails } from "../controllers/userControllers/userAllVehiclesController.js";
import {
  getVehiclesWithoutBooking,
  filterVehicles,
  showOneofkind,
  showAllVariants,
  BookCar,
  razorpayOrder,
  latestbookings,
  sendBookingDetailsEamil,
  findBookingsOfUser,
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
router.post("/sendBookingDetailsEamil", sendBookingDetailsEamil);
router.post("/findBookingsOfUser", findBookingsOfUser);

export default router;
