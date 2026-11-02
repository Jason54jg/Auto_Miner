package com.jason54jg.autominer

/** Comportement face à un tunnel bloqué (pioche AOE, surplombs, murs latéraux). */
enum class ObstacleNavigationMode {
    /** Avance uniquement en ligne droite (comportement d'origine). */
    FORWARD_ONLY,
    /** Contourne et descend d'un bloc si la config l'autorise. */
    SMART
}
