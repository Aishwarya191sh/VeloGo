import vehicle from "../../models/vehicleModel.js";
import Booking from "../../models/BookingModel.js"; // Resolved missing import bug in reference
import { errorHandler } from "../../utils/error.js";

// show all vehicles to user
export const listAllVehicles = async (req, res, next) => {
  try {
    const vehicles = await vehicle.find({
      isAdminApproved: true,
      isRejected: { $ne: true },
      isDeleted: { $ne: "true" }
    });

    if (!vehicles) {
      return next(errorHandler(404, "no vehicles found"));
    }
    res.status(200).json(vehicles);
  } catch (error) {
    console.error("List vehicles error:", error);
    next(errorHandler(500, "something went wrong"));
  }
};

// show one vehicle Detail to user
export const showVehicleDetails = async (req, res, next) => {
  try {
    if (!req.body) {
      return next(errorHandler(409, "body cannot be empty"));
    }
    const { id } = req.body;
    const vehicleDetail = await vehicle.findById(id);

    if (!vehicleDetail) {
      return next(errorHandler(404, "no vehicles found"));
    }

    res.status(200).json(vehicleDetail);
  } catch (error) {
    console.error("Get vehicle details error:", error);
    next(errorHandler(500, "something went wrong"));
  }
};

// check vehicle availability
export const checkAvailability = async (req, res, next) => {
  try {
    if (!req.body) {
      return next(errorHandler(401, "bad request no body"));
    }
    const { pickupDate, dropOffDate, vehicleId } = req.body;

    if (!pickupDate || !dropOffDate || !vehicleId) {
      return next(errorHandler(409, "pickup , dropffdate and vehicleId is required"));
    }

    // Check if pickupDate is before dropOffDate
    if (new Date(pickupDate) >= new Date(dropOffDate)) {
      return next(errorHandler(409, "Invalid date range"));
    }

    const sixHoursLater = new Date(dropOffDate);
    sixHoursLater.setTime(sixHoursLater.getTime() + 6 * 60 * 60 * 1000);

    // checking database find overlapping pickup and dropoffDates
    const existingBookings = await Booking.find({
      vehicleId,
      $or: [
        { pickupDate: { $lt: dropOffDate }, dropOffDate: { $gt: pickupDate } }, // Overlap condition
        { pickupDate: { $gte: pickupDate, $lt: dropOffDate } }, // Start within range
        { dropOffDate: { $gt: pickupDate, $lte: dropOffDate } }, // End within range
        {
          pickupDate: { $lte: pickupDate },
          dropOffDate: { $gte: dropOffDate },
        }, // Booking includes the entire time range
        {
          pickupDate: { $gte: sixHoursLater },
        },
      ],
    });

    // If there are overlapping bookings, return an error
    if (existingBookings.length > 0) {
      return next(
        errorHandler(
          400,
          "Vehicle is not available for the specified time period"
        )
      );
    }

    // If no overlapping bookings, vehicle is available
    return res
      .status(200)
      .json({ message: "Vehicle is available for booking" });
  } catch (error) {
    console.error("Check availability error:", error);
    next(errorHandler(500, "error in checkAvailability"));
  }
};

// search car filter in homepage
export const searchCar = async (req, res, next) => {
  try {
    if (req && req.body) {
      const {
        pickup_district,
        pickup_location,
        dropoff_location,
        pickuptime,
        dropofftime,
      } = req.body;

      // checking if dropOfftime is before or equals to pickupTime
      const pickuptimeDate = new Date(pickuptime.$d || pickuptime);
      const dropofftimeDate = new Date(dropofftime.$d || dropofftime);
      
      // Calculate the difference in milliseconds between two dates
      const dateDifferenceInMilliseconds =
        dropofftimeDate.getTime() - pickuptimeDate.getTime();
      // Convert milliseconds to days
      const dateDifferenceInDays =
        dateDifferenceInMilliseconds / (1000 * 60 * 60 * 24);

      if (dropofftimeDate <= pickuptimeDate || dateDifferenceInDays < 1) {
        return next(errorHandler(401, "dropoff date should be larger"));
      } else {
        const search = await vehicle.aggregate([
          {
            $match: {
              isDeleted: "false",
              isAdminApproved: true,
              isRejected: { $ne: true }
            },
          },
          {
            $match: {
              district: pickup_district,
              location: pickup_location,
              isBooked: false, // matching boolean in DB
            },
          },
          {
            $group: {
              _id: {
                model: "$model",
                location: "$location",
                fuel_type: "$fuel_type",
                transmition: "$transmition",
                seats: "$seats",
              },
              vehicles: {
                $push: "$$ROOT",
              },
            },
          },
          {
            $project: {
              _id: 1,
              vehicles: {
                $cond: {
                  if: {
                    $gt: [
                      {
                        $size: "$vehicles",
                      },
                      1,
                    ],
                  },
                  then: {
                    $arrayElemAt: ["$vehicles", 0],
                  },
                  else: "$vehicles",
                },
              },
            },
          },
          {
            $unwind: {
              path: "$vehicles",
            },
          },
          {
            $replaceRoot: {
              newRoot: "$vehicles",
            },
          },
        ]);
        if (search) {
          res.status(200).json(search);
        } else {
          res.status(404).json({ message: "no car found" });
        }
      }
    } else {
      res.status(400).json({ message: "please provide all the details" });
    }
  } catch (error) {
    console.error("Search car error:", error);
    next(errorHandler(500, "something went wrong while Searching car"));
  }
};
