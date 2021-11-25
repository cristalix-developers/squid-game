package me.func.top

import me.func.user.User

enum class BestUser(val title: String, val compare: Comparator<User>, val get: (User) -> Any) {

    FIRST_DEAD("Умер первым", compareBy<User> { !it.firstDead }, User::firstDead),
    KILLS("Убил %s\nигрока(ов)", compareBy { it.kills }, User::kills),
    BLOCK_BREAK("Сломал %s\nблока(ов)", compareBy { it.blockBreak }, User::blockBreak),
    DROWN_TIME("Вырезал фигуру\nза %s сек.", compareBy { -it.drownTime }, User::drownTime),
    TUGS("Стащил вниз\n%s игроков", compareBy { it.tugs }, User::tugs),
    DEATH_RUN("Поднялся по\nлестнице за\n%s сек.", compareBy { -it.timeOnDeathRun }, User::timeOnDeathRun),
    HERO("Герой", compareBy { !it.hero }, User::hero),
    SNOWBALLS("Воскрес %s\nраз", compareBy { it.respawn }, User::respawn),
    GREEN_LIGHT("Добрался до\nлинии за %s\nсек.", compareBy { -it.timeOnGreenLight }, User::timeOnGreenLight),;

}