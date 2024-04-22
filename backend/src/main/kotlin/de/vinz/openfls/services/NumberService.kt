package de.vinz.openfls.services

import de.vinz.openfls.services.NumberService.Companion.roundHalfToEven

class NumberService {
    companion object {
        fun roundDoubleToTwoDigits(value: Double): Double {
            return String.format("%.2f", value).toDouble()
        }

        fun convertDoubleToTimeDouble(value: Double): Double {
            val fullHours = value.toInt()
            val restMinutes =  ((value - fullHours) * 60).roundHalfToEven()
            val addedHours = restMinutes / 60
            val minutes = (restMinutes % 60).toDouble() / 100

            return roundDoubleToTwoDigits(fullHours.toDouble() + addedHours + minutes)
        }

        fun convertTimeDoubleToDouble(value: Double): Double {
            val fullHours = value.toInt()
            val restMinutes =  ((value - fullHours) * 100).roundHalfToEven()
            val addedHours = restMinutes / 100
            val minutes = (restMinutes % 100).toDouble() / 60

            return roundDoubleToTwoDigits(fullHours.toDouble() + addedHours + minutes)
        }

        fun convertMinutesToTimeDouble(value: Int): Double {
            val minutes = (value % 60).toDouble() / 100
            val hours = value / 60

            return roundDoubleToTwoDigits(hours.toDouble() + minutes)
        }

        fun sumTimeDoubles(sum1: Double, sum2: Double): Double {
            val totalMinutes = timeToMinutes(sum1) + timeToMinutes(sum2)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            return (hours + minutes / 100.0).toTwoDecimalPlaces()
        }

        fun diffTimeDoubles(sum1: Double, sum2: Double): Double {
            val minutes1 = timeToMinutes(sum1)
            val minutes2 = timeToMinutes(sum2)
            val difference = minutes1 - minutes2

            val hours = difference / 60
            val minutes = difference % 60
            return (hours.toDouble() + minutes / 100.0).toTwoDecimalPlaces()
        }

        private fun timeToMinutes(time: Double): Int {
            val hours = time.toInt()
            val minutes = ((time - hours) * 100).roundHalfToEven()
            return hours * 60 + minutes
        }

        private fun Double.toTwoDecimalPlaces(): Double = String.format("%.2f", this).toDouble()

        private fun Double.roundHalfToEven(): Int {
            val floorValue = this.toInt()
            val fractionalPart = this - floorValue

            return when {
                fractionalPart < 0.5 -> floorValue
                fractionalPart > 0.5 -> floorValue + 1
                else -> if (floorValue % 2 == 0) floorValue else floorValue + 1
            }
        }
    }
}