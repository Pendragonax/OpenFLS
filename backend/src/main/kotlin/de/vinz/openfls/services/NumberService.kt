package de.vinz.openfls.services

import kotlin.math.roundToInt

class NumberService {
    companion object {
        fun roundDoubleToTwoDigits(value: Double): Double {
            return String.format("%.2f", value).toDouble()
        }

        fun convertDoubleToTimeDouble(value: Double): Double {
            val intValue = value.toInt();
            var decimal = value - intValue;
            decimal *= 0.6;

            return roundDoubleToTwoDigits(intValue + decimal)
        }

        fun convertTimeDoubleToDouble(value: Double): Double {
            val intValue = value.toInt();
            var decimal = value - intValue;
            decimal *= (100.0 / 60.0);

            return roundDoubleToTwoDigits(intValue + decimal)
        }

        fun convertMinutesToTimeDouble(value: Int): Double {
            val minutes = (value % 60).toDouble() / 100
            val hours = value / 60

            return roundDoubleToTwoDigits(hours.toDouble() + minutes)
        }

        fun sumTimeDoubles(sum1: Double, sum2: Double): Double {
            val hourValue1 = sum1.toInt()
            val hourValue2 = sum2.toInt()
            val minuteValue1 = sum1 - hourValue1
            val minuteValue2 = sum2 - hourValue2
            val minutes = ((minuteValue1 + minuteValue2) * 100).roundToInt()
            val hoursFromMinutes = minutes / 60
            val restMinutes = minutes % 60
            val hours = hoursFromMinutes + hourValue1 + hourValue2

            return roundDoubleToTwoDigits(hours.toDouble() + (restMinutes.toDouble() / 100))
        }
    }
}