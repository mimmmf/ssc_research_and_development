package net.mcreator.sscresearchanddevelopment.component.subcomponents;

public enum NodeStatus {
    LOCKED,
    ACTIVE,
    EXHAUSTED,       // Триггер выполнен, реакция произошла
    COOLING_DOWN,    // Идет восстановление после реакции
    READY_TO_EXTRACT, // Можно забирать очки
    SOLVED,           // Очки получены, узел пройден
    PRIMED          // Активирован, ждёт реакции
}
