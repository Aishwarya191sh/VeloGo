import express from "express";
import { insertDummyData, getCarModelData } from "../controllers/adminControllers/masterCollectionController.js";

const router = express.Router();

router.get("/dummyData", insertDummyData);
router.get("/getVehicleModels", getCarModelData);

export default router;
