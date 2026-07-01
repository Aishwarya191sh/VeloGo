import express from "express";
import { insertDummyData, getCarModelData } from "../controllers/adminControllers/masterCollectionController.js";
import { changeStatus, allBookings } from "../controllers/adminControllers/bookingsController.js";
import { adminAuth } from "../controllers/adminControllers/adminController.js";
import { signIn } from "../controllers/authController.js";
import { signOut } from "../controllers/userControllers/userController.js";
import { addProduct, deleteVehicle, editVehicle, showVehicles } from "../controllers/adminControllers/dashboardController.js";
import { multerUploads } from "../utils/multer.js";
import { approveVendorVehicleRequest, fetchVendorVehilceRequests, rejectVendorVehicleRequest } from "../controllers/adminControllers/vendorVehilceRequests.js";

const router = express.Router();

router.get("/dummyData", insertDummyData);
router.get("/getVehicleModels", getCarModelData);
router.post("/changeStatus", changeStatus);

// Admin Module Routes
router.post("/dashboard", signIn, adminAuth);
router.get("/signout", signOut);
router.post("/addProduct", multerUploads, addProduct);
router.get("/showVehicles", showVehicles);
router.delete("/deleteVehicle/:id", deleteVehicle);
router.put("/editVehicle/:id", editVehicle);
router.get("/fetchVendorVehilceRequests", fetchVendorVehilceRequests);
router.post("/approveVendorVehicleRequest", approveVendorVehicleRequest);
router.post("/rejectVendorVehicleRequest", rejectVendorVehicleRequest);
router.get("/allBookings", allBookings);

export default router;
