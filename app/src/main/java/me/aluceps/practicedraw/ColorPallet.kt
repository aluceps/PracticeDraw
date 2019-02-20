package me.aluceps.practicedraw

import android.support.annotation.IdRes

enum class ColorPallet(@IdRes val resId: Int) {
    Red(R.color.colorTypeRed),
    Orange(R.color.colorTypeOrange),
    Yellow(R.color.colorTypeYellow),
    Green(R.color.colorTypeGreen),
    Purple(R.color.colorTypePurple),
    Blue(R.color.colorTypeBlue),
    LightBlue(R.color.colorTypeLightBlue),
    Black(R.color.colorTypeBlack),
    White(R.color.colorTypeWhite)
}