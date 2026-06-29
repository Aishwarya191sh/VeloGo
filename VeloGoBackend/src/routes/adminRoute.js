import express from "express";
import { insertDummyData, getCarModelData } from "../controllers/adminControllers/masterCollectionController.js";
import { changeStatus } from "../controllers/adminControllers/bookingsController.js";

const router = express.Router();

router.get("/dummyData", insertDummyData);
router.get("/getVehicleModels", getCarModelData);
router.post("/changeStatus", changeStatus);

export default router;
