package de.vinz.openfls.services

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

class TimeDoubleService {
    companion object {
        fun roundDoubleToTwoDigits(value: Double): Double {
            return String.format("%.2f", value).toDouble()
        }

        fun convertDoubleToTimeDouble(value: Double): Double {
            val sign = if (value < 0) -1 else 1

            // Gesamtminuten runden (HALF_EVEN) – am besten als Long/Int
            val totalMinutes = (abs(value) * 60).roundHalfToEven()

            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            val result = sign * (hours + minutes / 100.0)
            return roundDoubleToTwoDigits(result)
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

            val sign = if (totalMinutes < 0) -1 else 1
            val absMinutes = abs(totalMinutes)

            val hours = absMinutes / 60
            val minutes = absMinutes % 60

            val result = sign * (hours + minutes / 100.0)
            return result.toTwoDecimalPlaces()
        }

        fun diffTimeDoubles(sum1: Double, sum2: Double): Double {
            val minutes1 = timeToMinutes(sum1)
            val minutes2 = timeToMinutes(sum2)
            val difference = minutes1 - minutes2

            val hours = difference / 60
            val minutes = difference % 60
            return (hours.toDouble() + minutes / 100.0).toTwoDecimalPlaces()
        }

        fun multiplyTimeDoubles(sum1: Double, sum2: Double): Double {
            val minutes1 = timeToMinutes(sum1)
            val minutes2 = timeToMinutes(sum2)
            val difference = minutes1 * minutes2

            val hours = difference / 60
            val minutes = difference % 60
            return (hours.toDouble() + minutes / 100.0).toTwoDecimalPlaces()
        }

        fun divideTimeDoubles(sum1: Double, sum2: Double): Double {
            val minutes1 = timeToMinutes(sum1)
            val minutes2 = timeToMinutes(sum2)
            val difference = minutes1 / minutes2

            val hours = difference / 60
            val minutes = difference % 60
            return (hours.toDouble() + minutes / 100.0).toTwoDecimalPlaces()
        }

        private fun timeToMinutes(time: Double): Int {
            val sign = if (time < 0) -1 else 1

            // exakt auf 2 Nachkommastellen bringen (damit aus 39.5399999 -> 39.54 wird)
            val bd = BigDecimal.valueOf(abs(time)).setScale(2, RoundingMode.HALF_EVEN)

            val hours = bd.setScale(0, RoundingMode.DOWN).toInt()
            val minutes = bd.remainder(BigDecimal.ONE)
                .movePointRight(2)              // .54 -> 54
                .setScale(0, RoundingMode.HALF_EVEN)
                .toInt()

            require(minutes in 0..59) { "Ungültige Minutenanteil: $minutes (aus $time)" }

            return sign * (hours * 60 + minutes)
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