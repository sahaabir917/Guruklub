package com.gmpire.guruklub.util

import com.gmpire.guruklub.R

object ColorUtil {
    fun getColorByPosition(position: Int): Int {
        val colorVal = position % 5
        var color = 0

        when (colorVal) {
            0 -> {
                color =
                    R.color.lightOrange
            }
            1 -> {
                color =
                    R.color.paleLime
            }
            2 -> {
                color =
                    R.color.green
            }
            3 -> {
                color =
                    R.color.indigo
            }
            4 ->{
                color = R.color.yellow
            }
        }
        return color
    }

    fun getColorForRightWrong(answer: String): Int {
        val colorVal = answer
        var color = R.color.colorPrimaryDark

        when (colorVal) {
            "Right" -> {
                color =
                    R.color.colorPrimaryNew
            }
            "Wrong" -> {
                color =
                    R.color.wrongRed
            }
            "No answer" -> {
                color = R.color.paleOrange
            }
        }
        return color
    }

    fun getColorForColumn(answer: String): Int {
        val colorVal = answer
        var color = R.color.colorPrimaryDark

        when (colorVal) {
            "1st" -> {
                color =
                    R.color.light_grey
            }
            "2nd" -> {
                color =
                    R.color.column2color
            }
        }
        return color
    }


    fun getColorForableordisable(answer: String): Int {
        val colorVal = answer
        var color = R.color.colorPrimaryDark

        when (colorVal) {
            "Able" -> {
                color =
                    R.color.colorAccentNew
            }
            "Disable" -> {
                color =
                    R.color.newGray
            }
            "youtubebtnable" -> {
                color = R.color.colorAccentNew
            }
            "youtubebtndisable" ->{
                color = R.color.disabledbtncolor
            }
            "infocenterbtnAble" ->{
                color = R.color.lightAsh
            }
            "infocenterbtnDisable" ->{
                color = R.color.lightBrownFilter
            }
        }
        return color
    }




    fun getColorForPerformance(performanceStatus: String): Int {
        val colorVal = performanceStatus
        var color = R.color.colorPrimaryDark

        when (colorVal) {
            "Super" -> {
                color = R.color.green
            }

            "Good" -> {
                color =
                    R.color.green
            }
            "Average" -> {
                color =
                    R.color.mediumPerformance
            }
            "Weak" -> {
                color =
                    R.color.weakPerformance
            }

        }
        return color
    }


    fun getColorSuccessGraph(position: Int): Int {
        val colorVal = position % 6
        var color = 0

        when (colorVal) {
            0 -> {
                color =
                    R.color.purpleGraph
            }
            1 -> {
                color =
                    R.color.brightOrange
            }
            2 -> {
                color =
                    R.color.wrongRed
            }
            3 -> {
                color =
                    R.color.greenGraph
            }
            4 -> {
                color =
                    R.color.green
            }
            5 -> {
                color =
                    R.color.blueGraph
            }
        }
        return color
    }

    fun getColorForFilter(position: Int): Int {
        val colorVal = position % 5
        var color = 0

        when (colorVal) {
            0 -> {
                color =
                    R.color.lightBrownFilter
            }
            1 -> {
                color =
                    R.color.lightGreenFilter
            }
            2 -> {
                color =
                    R.color.lightTealFilter
            }
            3 -> {
                color =
                    R.color.lightBlueFilter
            }
            4 -> {
                color =
                    R.color.lightPinkFilter
            }
        }
        return color
    }


    fun getColorForFilter(): Int {
        var color = 0
        color = R.color.colorPrimaryNew
        return color
    }

    fun getRightWrongNoAnswer(answer: String): Int {
        val colorVal = answer
        var color = R.color.colorPrimaryDark

        when (colorVal) {
            "Right" -> {
                color =
                    R.color.paleBlue
            }
            "Wrong" -> {
                color =
                    R.color.darkOrange
            }
            "No answer" -> {
                color = R.color.paleOrange
            }
        }
        return color
    }

    fun getSubscriptionColorByPosition(position: Int): Int {
        val colorVal = position % 3
        var color = 0

        when (colorVal) {
            0 -> {
                color =
                    R.color.subscriptionbg1
            }
            1 -> {
                color =
                    R.color.lightOrange
            }
            2 -> {
                color =
                    R.color.subscriptionbg3
            }
        }
        return color
    }

    fun getColorForSubject(position: Int): Int {
        val colorVal = position % 4
        var color = 0

        when (colorVal) {
            0 -> {
                color =
                    R.color.palePurple
            }
            1 -> {
                color =
                    R.color.paleLime
            }
            2 -> {
                color =
                    R.color.lightRed
            }
            3 -> {
                color =
                    R.color.green
            }
        }
        return color
    }

}