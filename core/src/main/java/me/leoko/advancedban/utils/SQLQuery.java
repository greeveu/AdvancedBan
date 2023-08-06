package me.leoko.advancedban.utils;

/**
 * Created by Leo on 29.07.2017.
 */
public enum SQLQuery {
    CREATE_TABLE_PUNISHMENT(
        "CREATE TABLE `advancedban_punishments`" +
            "(" +
            "    `id`             INT         NOT NULL AUTO_INCREMENT," +
            "    `name`           INT         NOT NULL," +
            "    `uuid`           INT         NOT NULL," +
            "    `reason`         INT         NOT NULL DEFAULT ''," +
            "    `operator`       INT         NOT NULL DEFAULT 'undefined'," +
            "    `punishmentType` INT         NULL     DEFAULT NULL," +
            "    `start`          INT         NOT NULL," +
            "    `end`            INT         NOT NULL DEFAULT -1," +
            "    `calculation`    INT         NULL     DEFAULT NULL," +
            "    `archived`       BOOLEAN     NOT NULL DEFAULT FALSE," +
            "    `ip`             VARCHAR(64) NULL     DEFAULT NULL," +
            "    PRIMARY KEY (`id`)," +
            "    INDEX `idx_punishments_uuid_archived` (`uuid`, `archived`)" +
            ") ENGINE = InnoDB;"
    ),
    INSERT_PUNISHMENT(
        "INSERT INTO `advancedban_punishments` " +
            "(`name`, `uuid`, `ip`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    SELECT_EXACT_PUNISHMENT("SELECT * FROM `advancedban_punishments` WHERE `uuid` = ? AND `start` = ? AND `punishmentType` = ? AND archived = 0"),
    DELETE_PUNISHMENT("UPDATE `advancedban_punishments` SET archived = 1 WHERE `id` = ?"),
    DELETE_OLD_PUNISHMENTS("UPDATE `advancedban_punishments` SET archived = 0 WHERE `end` <= ? AND `end` != -1"),
    SELECT_USER_PUNISHMENTS("SELECT * FROM `advancedban_punishments` WHERE `uuid` = ? AND archived = 0"),
    SELECT_USER_PUNISHMENTS_HISTORY("SELECT * FROM `advancedban_punishments` WHERE `uuid` = ? AND archived = 1 ORDER BY `start` DESC"),
    SELECT_USER_PUNISHMENTS_WITH_IP("SELECT * FROM `advancedban_punishments` WHERE (`uuid` = ? OR `ip` = ?) AND archived = 0"),
    SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP("SELECT * FROM `advancedban_punishments` WHERE (`uuid` = ? OR `ip` = ?) AND archived = 1 ORDER BY `start` DESC"),
    REMOVE_IPS_FROM_ARCHIVED_PUNISHMENTS("UPDATE `advancedban_punishments` SET ip = 'REDACTED' WHERE ip <> NULL AND archived = 1"),
    SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION("SELECT * FROM `advancedban_punishments` WHERE `uuid` = ? AND `calculation` = ? AND archived = 1 ORDER BY `start` DESC"),
    UPDATE_PUNISHMENT_REASON("UPDATE `advancedban_punishments` SET `reason` = ? WHERE `id` = ? AND archived = 0"), //TODO: Discuss, if we need this to be "archived = 0" or if we can expand it to every punishment
    SELECT_PUNISHMENT_BY_ID("SELECT * FROM `advancedban_punishments` WHERE `id` = ? AND archived = 0"), //TODO: Discuss, if we need this to be "archived = 0" or if we can expand it to every punishment
    SELECT_ALL_PUNISHMENTS_LIMIT("SELECT * FROM `advancedban_punishments` WHERE archived = 0 ORDER BY `start` DESC LIMIT ?"),
    SELECT_ALL_PUNISHMENTS_HISTORY_LIMIT("SELECT * FROM `advancedban_punishments` WHERE archived = 1 ORDER BY `start` DESC LIMIT ?");

    private final String mysql;

    SQLQuery(String mysql) {
        this.mysql = mysql;
    }

    @Override
    public String toString() {
        return mysql;
    }
}
