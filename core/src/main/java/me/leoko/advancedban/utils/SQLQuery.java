package me.leoko.advancedban.utils;

/**
 * Created by Leo on 29.07.2017.
 */
public enum SQLQuery {
    CREATE_TABLE_PUNISHMENT(
        "CREATE TABLE IF NOT EXISTS `Punishments` (" +
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`uuid` VARCHAR(35) NULL DEFAULT NULL," +
            "`reason` VARCHAR(255) NULL DEFAULT NULL," +
            "`operator` VARCHAR(16) NULL DEFAULT NULL," +
            "`punishmentType` VARCHAR(16) NULL DEFAULT NULL," +
            "`start` LONG DEFAULT NULL," +
            "`end` LONG DEFAULT NULL," +
            "`calculation` VARCHAR(50) NULL DEFAULT NULL," +
            "PRIMARY KEY (`id`))"
    ),
    CREATE_TABLE_PUNISHMENT_HISTORY(
        "CREATE TABLE IF NOT EXISTS `PunishmentHistory` (" +
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`uuid` VARCHAR(35) NULL DEFAULT NULL," +
            "`reason` VARCHAR(255) NULL DEFAULT NULL," +
            "`operator` VARCHAR(16) NULL DEFAULT NULL," +
            "`punishmentType` VARCHAR(16) NULL DEFAULT NULL," +
            "`start` LONG DEFAULT NULL," +
            "`end` LONG DEFAULT NULL," +
            "`calculation` VARCHAR(50) NULL DEFAULT NULL," +
            "PRIMARY KEY (`id`))"
    ),
    INSERT_PUNISHMENT(
        "INSERT INTO `Punishments` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    INSERT_PUNISHMENT_HISTORY(
        "INSERT INTO `PunishmentHistory` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    SELECT_EXACT_PUNISHMENT("SELECT * FROM `Punishments` WHERE `uuid` = ? AND `start` = ? AND `punishmentType` = ?"),
    DELETE_PUNISHMENT("DELETE FROM `Punishments` WHERE `id` = ?"),
    DELETE_OLD_PUNISHMENTS("DELETE FROM `Punishments` WHERE `end` <= ? AND `end` != -1"),
    SELECT_USER_PUNISHMENTS("SELECT * FROM `Punishments` WHERE `uuid` = ?"),
    SELECT_USER_PUNISHMENTS_HISTORY("SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? ORDER BY `start` DESC"),
    SELECT_USER_PUNISHMENTS_WITH_IP("SELECT * FROM `Punishments` WHERE `uuid` = ? OR `uuid` = ?"),
    SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP("SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? OR `uuid` = ? ORDER BY `start` DESC"),
    SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION("SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? AND `calculation` = ? ORDER BY `start` DESC"),
    UPDATE_PUNISHMENT_REASON("UPDATE `Punishments` SET `reason` = ? WHERE `id` = ?"),
    SELECT_PUNISHMENT_BY_ID("SELECT * FROM `Punishments` WHERE `id` = ?"),
    SELECT_ALL_PUNISHMENTS_LIMIT("SELECT * FROM `Punishments` ORDER BY `start` DESC LIMIT ?"),
    SELECT_ALL_PUNISHMENTS_HISTORY_LIMIT("SELECT * FROM `PunishmentHistory` ORDER BY `start` DESC LIMIT ?");

    private final String mysql;

    SQLQuery(String mysql) {
        this.mysql = mysql;
    }

    @Override
    public String toString() {
        return mysql;
    }
}
