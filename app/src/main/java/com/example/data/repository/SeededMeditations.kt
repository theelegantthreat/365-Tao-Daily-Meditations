package com.example.data.repository

import com.example.data.model.TaoMeditation

object SeededMeditations {
    fun getMeditation(day: Int): TaoMeditation {
        return when (day) {
            1 -> TaoMeditation(
                day = 1,
                title = "The Eternal Way",
                verse = "The Tao that can be spoken of is not the eternal Tao.\nThe name that can be named is not the eternal name.\nThe nameless is the origin of Heaven and Earth.\nThe named is the mother of all things.",
                commentary = "Begin your journey of 365 days by letting go of labels. The Way (Tao) is not a set of rigid doctrines, but the underlying flow of the entire universe. When we try to define everything, we lose touch with the mystery.\n\nToday, try to observe without naming, feel without defining, and exist without force. Let the mind settle into its natural state of peaceful awareness."
            )
            2 -> TaoMeditation(
                day = 2,
                title = "The Flow of Water",
                verse = "The highest good is like water.\nWater benefits all things without competing with them.\nIt flows to the low places that others disdain.\nTherefore, it is close to the Tao.",
                commentary = "Water is the ultimate symbol of Taoist wisdom. It is soft, yet it can wear away the hardest rock. It does not struggle; it simply finds the path of least resistance.\n\nToday, when you encounter an obstacle, do not meet it with force. Flow around it, adapt, and remain humble. True strength lies in gentleness."
            )
            3 -> TaoMeditation(
                day = 3,
                title = "The Uncarved Block",
                verse = "Simplicity has no name.\nFree from desire, it is quiet.\nIn quietness, the world finds peace on its own.\nBe like the uncarved block of wood.",
                commentary = "Before we are shaped by society's expectations, we are full of infinite potential. The 'uncarved block' (Pu) represents our natural, simple, and untouched state.\n\nToday, shed your roles, titles, and achievements. Reconnect with your simple, quiet core. You are already complete just as you are."
            )
            4 -> TaoMeditation(
                day = 4,
                title = "The Wisdom of Emptiness",
                verse = "We shape clay into a pot,\nbut it is the emptiness inside that holds the water.\nWe hammer wood for a house,\nbut it is the empty space inside that makes it livable.\nTherefore, realize that being is useful, but non-being is essential.",
                commentary = "We often value only what is busy, full, and active. But it is the quiet, empty spaces that give life its utility and peace. A room needs space to be lived in; a mind needs quietness to understand.\n\nToday, create gaps of silence. Empty your mind of worries and make room for peace to enter."
            )
            5 -> TaoMeditation(
                day = 5,
                title = "Action through Inaction",
                verse = "The Sage does not act, yet nothing is left undone.\nThey teach without words,\nand lead by setting an example.\nWhen work is done, they do not cling to it.",
                commentary = "This is Wu Wei, or effortless action. It means acting in total alignment with the natural flow of things, rather than forcing outcomes with personal ego.\n\nToday, try to act without forcing. Do your work with focus, but release your grip on the results. Let life unfold naturally."
            )
            6 -> TaoMeditation(
                day = 6,
                title = "Returning to the Source",
                verse = "All things arise and flourish,\nthen each returns to its root.\nReturning to the root is peace.\nThis is the path of destiny, the law of eternity.",
                commentary = "Nature moves in cycles. Trees bloom and shed leaves, water evaporates and returns as rain. Returning to our root means returning to silence and calm reflection after activity.\n\nAt the end of your day, take time to return to your source. Settle your energy and rest in the profound stillness of the present moment."
            )
            7 -> TaoMeditation(
                day = 7,
                title = "Living in the Present",
                verse = "If you are depressed, you are living in the past.\nIf you are anxious, you are living in the future.\nIf you are at peace, you are living in the present.\nSettle into the here and now.",
                commentary = "The mind loves to wander into memories or anticipate problems. But life is only ever found in this exact breath.\n\nToday, pull your awareness back whenever it drifts. Look at the sky, feel your feet on the floor, and breathe deeply. Peace is always right here, waiting."
            )
            8 -> TaoMeditation(
                day = 8,
                title = "The Valley Spirit",
                verse = "The valley spirit never dies;\nit is called the mysterious feminine.\nThe gateway of the mysterious feminine\nis the root of Heaven and Earth.\nIt is like a continuous thread, used without effort.",
                commentary = "The valley spirit represents receptivity, openness, and nurturing. A valley does not reach for the sky; it waits at the bottom, receiving the rain and giving rise to life.\n\nToday, practice receiving. Listen more than you speak. Let go of the need to control or conquer, and enjoy the fertile peace of being receptive."
            )
            9 -> TaoMeditation(
                day = 9,
                title = "Letting Go of Ego",
                verse = "He who stands on tiptoe is not steady.\nHe who strides too far cannot keep pace.\nHe who displays himself does not shine.\nHe who asserts himself is not distinguished.",
                commentary = "Ego wants to make us look tall and superior, but standing on tiptoe only makes us unstable. True confidence is grounded and needs no display.\n\nToday, step down from the pedestal. Walk at your natural pace. Allow yourself to be simple and unnoticed, and notice how much lighter you feel."
            )
            10 -> TaoMeditation(
                day = 10,
                title = "The Breath of Life",
                verse = "In concentrating your breath and achieving gentleness,\ncan you be like a newborn babe?\nIn washing and clearing your inner vision,\ncan you be without spot or blemish?",
                commentary = "A newborn breathes naturally with the whole body, free from tension or stress. To return to this breath is to return to the natural life force (Qi).\n\nToday, take three conscious, deep breaths. Let your belly rise and fall. Wash away the dust of mental chatter and look at the world with fresh, clear eyes."
            )
            else -> {
                // Poetic synthesized generation for offline backup
                val title = getOfflineTitle(day)
                val verse = getOfflineVerse(day)
                val commentary = getOfflineCommentary(day, title)
                TaoMeditation(day = day, title = title, verse = verse, commentary = commentary)
            }
        }
    }

    private fun getOfflineTitle(day: Int): String {
        val themes = listOf(
            "The Way of Serenity", "Harmony with Nature", "The Quiet Mind", "The Infinite Vessel",
            "Simplicity of Heart", "The Path of Balance", "Softness Overcomes Hardness", "The Centered Soul",
            "The Great Flow", "Abiding in Stillness", "The Natural Self", "Unbounded Awareness",
            "The Silent Sage", "Nourishing the Spirit", "The Empty Vessel", "The Gateway of Mystery"
        )
        return themes[day % themes.size] + " (Day " + String.format("%03d", day) + ")"
    }

    private fun getOfflineVerse(day: Int): String {
        val verses = listOf(
            "The universe is eternal because it does not live for itself.\nBy putting themselves last, the Sages find themselves first.\nBy being detached, they remain at one with all.",
            "He who knows that enough is enough\nwill always have enough.\nContentment is the greatest treasure;\nsimplify the heart and the path clears.",
            "Do your work and step back.\nThis is the only path to peace.\nLike the sun that shines and sets,\nlet your actions rise and fall without attachment.",
            "A tree that is unbending is easily broken.\nThe stiff and rigid will fall;\nthe soft and yielding will conquer.\nMaintain flexibility in all things.",
            "Knowing others is intelligence;\nknowing yourself is true wisdom.\nMastering others is strength;\nmastering yourself is true power.",
            "The Sage does not accumulate.\nThe more they give to others, the more they have.\nThe more they help others, the richer they become.\nThis is the true flow of abundance."
        )
        return verses[day % verses.size]
    }

    private fun getOfflineCommentary(day: Int, title: String): String {
        return "For Day $day, we meditate on '$title'. In the busy currents of life, it is easy to lose our center and get carried away by anxiety, desires, and opinions.\n\nBy taking a moment of pause today, you align yourself with the great cosmic flow. Relax your shoulders, let your breathing be soft and deep, and remember that you do not need to fight the river of life. Simply float, trust the current, and know that you are exactly where you need to be."
    }
}
