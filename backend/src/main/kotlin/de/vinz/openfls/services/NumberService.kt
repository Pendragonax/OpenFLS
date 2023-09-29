package de.vinz.openfls.services

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
    }
}