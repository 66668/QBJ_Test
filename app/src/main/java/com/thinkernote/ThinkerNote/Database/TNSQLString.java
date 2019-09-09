package com.thinkernote.ThinkerNote.Database;

public class TNSQLString {

    //----------------------------------------------------------------------------------------------------
// Setting Table
    public final static String SETTING_CREATE_TABLE =
            "CREATE TABLE `Setting`(" +
                    "`settingId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
                    ",`dbVersion` INTEGER NOT NULL" +
                    ",`protocolVersion` INTEGER NOT NULL" +
                    ")";

    public final static String SETTING_DROP_TABLE =
            "DROP TABLE `Setting`";

    //----------------------------------------------------------------------------------------------------
// `User` Table
    public final static String USER_CREATE_TABLE =
            "CREATE TABLE `User`(" +
                    "`username` TEXT(50) NOT NULL" +
                    ",`password` TEXT(32) NOT NULL" +
                    ",`userEmail` TEXT(100) NOT NULL" +
                    ",`phone` TEXT(100) NOT NULL" +
                    ",`userId` LONG NOT NULL" +
                    ",`emailVerify` INTEGER NOT NULL" +
                    ",`totalSpace` LONG NOT NULL" +
                    ",`usedSpace` LONG NOT NULL" +
                    ")";

    public final static String USER_INSERT =
            "INSERT INTO `User` " +
                    "(`username`,`password`,`userEmail`,`phone`,`userId`,`emailVerify`,`totalSpace`,`usedSpace`) " +
                    "VALUES (?,?,?,?,?)";

    public final static String USER_UPDATE =
            "UPDATE `User` SET `username`=?, `password`=?, `userEmail`=?, `phone`=?, `emailVerify`=?, `totalSpace`=?, `usedSpace`=? WHERE `userId`=?";

    public final static String USER_CHECK_USERNAME =
            "SELECT `userId` FROM `User` WHERE `username`=? OR `userEmail`=? OR `phone`=?";

    public final static String USER_UPDATE_NAME =
            "UPDATE `User` SET `username`=? WHERE `userId`=?";

    public final static String USER_UPDATE_PHONE =
            "UPDATE `User` SET `phone`=? WHERE `userId`=?";

    public final static String USER_UPDATE_PWD =
            "UPDATE `User` SET `password`=? WHERE `userId`=?";

    public final static String USER_UPDATE_EMAIL =
            "UPDATE `User` SET `userEmail`=? WHERE `userId`=?";

    public final static String USER_SELECT_BY_ID =
            "SELECT `username`, `password`, `userEmail`, `phone`, `userId`, `emailVerify`, `totalSpace`, `usedSpace` " +
                    "FROM `User` WHERE `userId`=?";

    public final static String USER_CLEAR =
            "DELETE FROM `User`";

    public final static String USER_DROP_TABLE =
            "DROP TABLE `User`";

    //------------------------------------------------------------------------------
// `Category` Table
    public final static String CAT_CREATE_TABLE =
            "CREATE TABLE `Category`(" +
                    "`catLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
                    ",`catName` TEXT(100) NOT NULL" +
                    ",`userId` LONG NOT NULL" +
                    ",`trash` INTEGER NOT NULL" +
                    ",`catId` LONG NOT NULL" +
                    ",`noteCounts` INTEGER NOT NULL DEFAULT 0" +
                    ",`catCounts` INTEGER NOT NULL DEFAULT 0" +
                    ",`deep` INTEGER NOT NULL DEFAULT 0" +
                    ",`pCatId` INTEGER NOT NULL DEFAULT 1" +
                    ",`isNew` INTEGER NOT NULL DEFAULT 0" +
                    ",`createTime` LONG NOT NULL DEFAULT 0" +
                    ",`lastUpdateTime` LONG NOT NULL DEFAULT 0" +
                    ",`strIndex` TEXT(8) NOT NULL DEFAULT ''" +
                    ")";

    public final static String CAT_INSERT =
            "INSERT INTO `Category` " +
                    "(`catName`,`userId`,`trash`,`catId`, `noteCounts`, `catCounts`," +
                    "`deep`, `pCatId`, `isNew`, `createTime`, `lastUpdateTime`, `strIndex`)" +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";

    public final static String CAT_UPDATE =
            "UPDATE `Category` " +
                    "SET `catName` = ?, `userId` = ?, `trash` = ?, `catId` = ?, `noteCounts` = ?, `catCounts` = ?, `deep` = ?, " +
                    "`pCatId` = ?, `isNew` = ?, `createTime` = ?, `lastUpdateTime` = ?, `strIndex` = ? " +
                    "WHERE `catId` = ?";

    public final static String CAT_SELECT_ALL =
            "SELECT * FROM `Category` WHERE `userId` = ? ORDER BY `strIndex` ASC ";

    public final static String CAT_SELECT_BY_PCATID =
            "SELECT * FROM `Category` WHERE `userId` = ? AND `pCatId` = ? ORDER BY `strIndex` ASC ";

    public final static String CAT_SELECT_BY_PCATID_UPDATETIME =
            "SELECT * FROM `Category` WHERE `userId` = ? AND `pCatId` = ? ORDER BY `lastUpdateTime` ASC ";

    public final static String CAT_SELECT_BY_PCATID_CREATETIME =
            "SELECT * FROM `Category` WHERE `userId` = ? AND `pCatId` = ? ORDER BY `createTime` DESC ";

    public final static String CAT_CHECK_CATID =
            "SELECT * FROM `Category` WHERE `catId` = ? ";

    public final static String CAT_COUNT_PCATID =
            "SELECT count(*) FROM `Category` WHERE `pCatId` = ? ";

    public final static String CAT_CHACK_NEW =
            "SELECT `catId` FROM `Category` " +
                    "WHERE `trash` = 0 AND `isNew` = 1";

    public final static String CAT_SET_ISNEW =
            "UPDATE `Category` " +
                    "SET `isNew` = 0 " +
                    "WHERE `catId` = ?";

    public final static String CAT_UPDATE_TRASH =
            "UPDATE `Category` SET `trash` = ? " +
                    "WHERE `catId` = ?";

    public final static String CAT_UPDATE_LASTUPDATETIME =
            "UPDATE `Category` SET `lastUpdateTime` = ? " +
                    "WHERE `catId` = ?";

    public final static String CAT_DELETE =
            "UPDATE `Category` SET `trash` = 1, `lastUpdateTime` = ? " +
                    "WHERE `catId` = ?";

    public final static String CAT_RENAME =
            "UPDATE `Category` " +
                    "SET `catName` = ? " +
                    "WHERE `catId` = ?";

    public final static String CAT_CLEAR =
            "DELETE FROM `Category` WHERE `userId` = ?";

    public final static String CAT_CLEAR_PCATID =
            "DELETE FROM `Category` WHERE `userId` = ? AND `pCatId` = ?";

    public final static String CAT_DELETE_CAT =
            "DELETE FROM `Category` WHERE `catId` = ?";

    public final static String CAT_DROP_TABLE =
            "DROP TABLE `Category`";

    //------------------------------------------------------------------------------
// `Tag` Table
    public final static String TAG_CREATE_TABLE =
            "CREATE TABLE `Tag`(" +
                    "`tagLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
                    ",`tagName` TEXT(50) NOT NULL" +
                    ",`userId` LONG NOT NULL" +
                    ",`trash` INTEGER NOT NULL" +
                    ",`tagId` LONG NOT NULL" +
                    ",`noteCounts` INTEGER NOT NULL" +
                    ",`strIndex` TEXT(8) NOT NULL DEFAULT ''" +
                    ")";

    public final static String TAG_INSERT =
            "INSERT INTO `Tag` " +
                    "(`tagName`,`userId`,`trash`,`tagId`,`noteCounts`,`strIndex`) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    public final static String TAG_UPDATE =
            "UPDATE `Tag` " +
                    "SET `tagName` = ?, `userId` = ?, `trash` = ?, " +
                    "`tagId` = ?, `noteCounts` = ?, `strIndex` = ? " +
                    "WHERE `tagId` = ?";

    public final static String TAG_GET =
            "SELECT * FROM `Tag` " +
                    "WHERE `tagId` = ? AND `userId` = ?";

    public final static String TAG_GET_BY_TAGNAME =
            "SELECT * FROM `Tag` " +
                    "WHERE `tagName` = ? AND `userId` = ?";

    public final static String TAG_GET_ALL =
            "SELECT * FROM `Tag` WHERE `userId` = ? ORDER BY `strIndex` ASC";

    public final static String TAG_UPDATA_INDEX =
            "UPDATE `Tag` " +
                    "SET `strIndex` = ? " +
                    "WHERE `tagId` = ? ";

    public final static String TAG_NAMECHECK =
            "SELECT `tagId`, `trash` FROM `Tag` " +
                    "WHERE `tagName` = ? AND `userId` = ?";

    public final static String TAG_UPDATE_TRASH =
            "UPDATE `Tag` SET `trash` = ? " +
                    "WHERE `tagId` = ?";

    public final static String TAG_RENAME =
            "UPDATE `Tag` " +
                    "SET `tagName` = ?, `strIndex` = ? " +
                    "WHERE `tagId` = ?";

    public final static String TAG_SET_NOTECOUNTS =
            "UPDATE `Tag` SET `noteCounts` = ? WHERE `tagId` = ?";

    public final static String TAG_CLEAR =
            "DELETE FROM `Tag` WHERE `userId` = ?";

    public final static String TAG_REAL_DELETE =
            "DELETE FROM `Tag` WHERE `tagId` = ?";

    public final static String TAG_DROP_TABLE =
            "DROP TABLE `Tag`";

    //------------------------------------------------------------------------------
// `Note` Table
    public final static String NOTE_CREATE_TABLE =
            "CREATE TABLE `Note`(" +
                    "`noteLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
                    ",`title` TEXT(100) NOT NULL" +
                    ",`userId` LONG NOT NULL" +
                    ",`catId` LONG NOT NULL" +
                    ",`content` TEXT(102400) NOT NULL" +
                    ",`trash` INTEGER NOT NULL" +
                    ",`source` TEXT(1000) NOT NULL" +
                    ",`createTime` LONG NOT NULL" +
                    ",`lastUpdate` LONG NOT NULL" +
                    ",`syncState` INTEGER NOT NULL" + //1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
                    ",`noteId` LONG NOT NULL" +
                    ",`shortContent` TEXT(102400) NOT NULL" +
                    ",`tagStr` TEXT(1000) NOT NULL" +
                    ",`lbsLongitude` INTEGER NOT NULL" +
                    ",`lbsLatitude` INTEGER NOT NULL" +
                    ",`lbsRadius` INTEGER NOT NULL" +
                    ",`lbsAddress` TEXT(200) NOT NULL" +
                    ",`nickName` TEXT(50) NOT NULL" +
                    ",`thumbnail` TEXT(1000) NOT NULL" +
                    ",`contentDigest` TEXT(200)" +// 修改 sjy 0702-->NOT NULL移除
                    ")";

    public final static String NOTE_INSERT =
            "INSERT INTO `Note` (`title`,`userId`,`catId`,`trash`,`content`,`source`," +
                    "`createTime`,`lastUpdate`,`syncState`,`noteId`,`shortContent`,`tagStr`,`lbsLongitude`," +
                    "`lbsLatitude`,`lbsRadius`,`lbsAddress`,`nickName`,`thumbnail`,`contentDigest`) " +
                    "VALUES" +
                    "(?,?,?,?,?,?," +
                    "?,?,?,?,?,?," +
                    "?,?,?,?,?,?,?)";

    public final static String NOTELOCALID_BY_NOTEID =
            "SELECT `noteLocalId` " +
                    "FROM `Note` " +
                    "WHERE `noteId` = ? ";

    public final static String NOTE_UPDATE_FOR_LIST =
            "UPDATE `Note` " +
                    "SET `title` = ?, `catId` = ?, " +
                    "`createTime` = ?, `lastUpdate` = ?, `syncState` = ?, " +
                    "`shortContent` = ?, `tagStr` = ?, `contentDigest` = ? " +
                    "WHERE `noteId` = ?";

    public final static String NOTE_UPDATE =
            "UPDATE `Note` " +
                    "SET `title` = ?, `catId` = ?, `content` = ?, " +
                    "`createTime` = ?, `lastUpdate` = ?, " +
                    "`shortContent` = ?, `tagStr` = ?, `contentDigest` = ?, `syncState` = ? " +
                    "WHERE `noteId` = ?";

    public final static String NOTE_LOCAL_UPDATE =
            "UPDATE `Note` " +
                    "SET `title` = ?, `catId` = ?, `content` = ?, " +
                    "`createTime` = ?, `lastUpdate` = ?, " +
                    "`shortContent` = ?, `tagStr` = ?, `contentDigest` = ?, `syncState` = ? " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTE_COUNT_TRASH =
            "SELECT count(`noteId`) " +
                    "FROM `Note` " +
                    "WHERE `trash` = ? AND `syncState` != 5 AND `userId` = ? ";

    public final static String NOTE_GET_ALL =
            "SELECT * " +
                    "FROM `Note`" +
                    "WHERE `trash` = 0 AND `syncState` != 5 AND `userId` = ? ";

    public final static String NOTE_GET_ALL_BY_FOLDER =
            "SELECT * " +
                    "FROM `Note`" +
                    "WHERE `trash` = 0 AND `syncState` != 5 AND `userId` = ? AND `catId` = ?";

    public final static String NOTE_COUNT_BYCAT =
            "SELECT count(*) FROM `Note` " +
                    "WHERE `trash` = 0 AND `syncState` != 5 AND `catId` = ? AND `userId` = ?";

    public final static String NOTE_COUNT_BYALL =
            "SELECT count(*) FROM `Note` " +
                    "WHERE `trash` = 0 AND `syncState` != 5 AND `userId` = ? ";

    public final static String NOTE_COUNT_BYTAGNAME =
            "SELECT count(*) FROM `Note` " +
                    "WHERE `trash` = 0 AND `syncState` != 5 AND (`tagStr` LIKE ?) AND `userId` = ? ";

    public final static String NOTE_GET_CATID =
            "SELECT `catId` FROM `Note` " +
                    "WHERE `noteId` = ?";

    public final static String NOTE_UPDATE_CONTENT =
            "UPDATE `Note` SET `content` = ?, `shortContent` = ? " +
                    "WHERE `noteId` = ?";

    public final static String NOTE_UPDATE_NOTEID_BY_NOTELOCALID =
            "UPDATE `Note` SET `noteId` = ?, `syncState` = 2 " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTELIST_BYTRASH =
            "SELECT `noteId`, `lastUpdate`, `createTime` " +
                    "FROM `Note` " +
                    "WHERE `userId` = ? AND `trash` = ? AND `syncState` != 5 " +
                    "ORDER BY `lastUpdate` DESC,`noteId` DESC ";

    public final static String NOTELIST_BYCAT =
            "SELECT `noteLocalId`, `noteId`, `lastUpdate`, `createTime` " +
                    "FROM `Note` WHERE `trash` = 0 AND `catId` = ? AND `syncState` != 5 " +
                    "ORDER BY `lastUpdate` DESC,`noteLocalId` DESC ";

    public final static String NOTELIST_BYKEYWORDS_UPDATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `syncState` != 5 AND `trash` != 2 " +
                    "AND (`title` LIKE ? OR `content` LIKE ?) " +
                    "ORDER BY `lastUpdate` DESC ";

    public final static String NOTELIST_BYKEYWORDS_CREATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `syncState` != 5 AND `trash` != 2 " +
                    "AND (`title` LIKE ? OR `content` LIKE ?) " +
                    "ORDER BY `createTime` DESC ";

    public final static String NOTELIST_BYSYNCSTATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `syncState` = ? ";

    public final static String NOTELIST_BYSYNCSTATE_BYCATID =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `syncState` = ? AND `catId` = ? ";

    public final static String NOTELIST_BYONE =
            "SELECT `noteLocalId`, `noteId`, `title`, `lastUpdate`, `syncState`, `createTime`, `shortContent`, `tagStr`, " +
                    "`nickName`, `noteId`, " +
                    "`thumbnail`, `contentDigest` " +
                    "FROM `Note` WHERE `noteLocalId` = ? ";

    public final static String NOTE_COUNT_KEYWORDS =
            "SELECT count(`noteLocalId`) " +
                    "FROM `Note` " +
                    "WHERE `trash` = 0 AND `userId` = ? " +
                    "AND (`title` LIKE ? OR `content` LIKE ? )";

    public final static String NOTE_ALLINFO =
            "SELECT `noteLocalId`, `title`, `userId`, `catId`, `content`, " +
                    "`trash`, `createTime`, " +
                    "`lastUpdate`, `syncState`, `noteId`, " +
                    "`shortContent`, `tagStr`, " +
                    "`lbsLongitude`, `lbsLatitude`, `lbsRadius`, `lbsAddress`, `nickName`, " +
                    "FROM `Note` WHERE `noteLocalId` = ?";

    public final static String NOTE_SIMPLE_INFO =
            "SELECT `noteLocalId`, `title`, `userId`, `catId`, `contentDigest`, " +
                    "`trash`, `syncState`, `noteId`, `revision`, " +
                    "`nickName` " +
                    "FROM `Note` WHERE `noteLocalId` = ?";

    public final static String NOTE_GET_COUNT_CREATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 0 AND `syncState` != 5 ORDER BY `createTime` DESC LIMIT ? ";

    public final static String NOTE_GET_COUNT_UPDATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 0 AND `syncState` != 5 ORDER BY `lastUpdate` DESC LIMIT ? ";

    public final static String NOTE_GET_BY_CATID_UPDATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `catId` = ? AND `trash` = 0 AND `syncState` != 5 ORDER BY `lastUpdate` DESC ";

    public final static String NOTE_GET_BY_CATID_CREATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `catId` = ? AND `trash` = 0 AND `syncState` != 5 ORDER BY `createTime` DESC ";

    public final static String NOTE_GET_COUNT_BY_TAGID_UPDATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 0 AND `syncState` != 5 AND (`tagStr` LIKE ?) ORDER BY `lastUpdate` DESC LIMIT ? ";

    public final static String NOTE_GET_COUNT_BY_TAGID_CREATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 0 AND `syncState` != 5 AND (`tagStr` LIKE ?) ORDER BY `createTime` DESC LIMIT ? ";

    public final static String NOTE_GET_COUNT_BY_CATID_UPDATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 0 AND `syncState` != 5 AND `catId` = ? ORDER BY `lastUpdate` DESC LIMIT ? ";

    public final static String NOTE_GET_COUNT_BY_CATID_CREATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 0 AND `syncState` != 5 AND `catId` = ? ORDER BY `createTime` DESC LIMIT ? ";

    public final static String NOTE_GET_All_BY_TRASH_UPDATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 2 AND `syncState` != 5 ORDER BY `lastUpdate` DESC ";

    public final static String NOTE_GET_All_BY_TRASH_CREATE =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `trash` = 2 AND `syncState` != 5 ORDER BY `createTime` DESC ";

    public final static String LASTUPDATE_BY_NOTEID =
            "SELECT `lastUpdate` FROM `Note` WHERE `noteLocalId` = ? ";

    public final static String NOTES_MOVE_CAT =
            "UPDATE `Note` SET `catId` = ?, `syncState` = 1 " +
                    "WHERE `catId` = ?";

    public final static String NOTES_REMOVE_FOR_CAT =
            "UPDATE `Note` SET `syncState` = 1, `trash` = ?, `catId` = ? " +
                    "WHERE `catId` = ?";

    public final static String NOTE_UPDATE_THUMBNAIL =
            "UPDATE `Note` SET `thumbnail`=? WHERE `noteLocalId`=?";

    public final static String NOTE_UPDATE_SYNCSTATE =
            "UPDATE `Note` SET `syncState`=? WHERE `noteLocalId`=?";

    public final static String NOTE_CAT_MOVE =
            "UPDATE `Note` " +
                    "SET `catId` = ? " +
                    "WHERE `catId` = ?";

    public final static String NOTE_MOVE_CAT =
            "UPDATE `Note` " +
                    "SET `catId` = ?, `syncState` = ?, `lastUpdate` = ? " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTE_CHANGE_CREATETIME =
            "UPDATE `Note` " +
                    "SET `createTime` = ?, `syncState` = ?, `lastUpdate` = ? " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTE_TRASH_CATID =
            "UPDATE `Note` " +
                    "SET `trash` = ?, `lastUpdate` = ?, `catId` = ? " +
                    "WHERE `catId` = ?";

    public final static String NOTE_CHANGE_TAG =
            "UPDATE `Note` " +
                    "SET `tagStr` = ?, `syncState` = ?, `lastUpdate` = ? " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTE_MOVE_USER =
            "UPDATE `Note` SET `userId` = ? WHERE `userId` = ? ";

    public final static String NOTE_BY_CAT =
            "SELECT `noteId` FROM `Note` " +
                    "WHERE `catId` = ? AND `trash` = 0";

    public final static String NOTE_BY_SYNCSTATE =
            "SELECT `noteId` FROM `Note` " +
                    "WHERE `syncState` != 0 AND `noteId` != -1 AND `catId` = ? " +
                    "AND `userId` = ? ";

    public final static String NOTE_CHECK_ID =
            "SELECT * FROM `Note` WHERE `noteId` = ?";

    public final static String NOTE_CHECK_LOCALID =
            "SELECT * FROM `Note` WHERE `userId` = ? AND `noteLocalId` = ?";

    public final static String NOTE_SET_TRASH =
            "UPDATE `Note` SET `trash` = ?, `syncState` = ?, `lastUpdate` = ? " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTE_SHORT_CONTENT =
            "UPDATE `Note` " +
                    "SET `shortContent` = ? " +
                    "WHERE `noteLocalId` = ?";

    public final static String NOTE_CLEAR =
            "DELETE FROM `Note` WHERE `userId` = ?";

    public final static String NOTE_DELETE_CAT =
            "DELETE FROM `Note` WHERE `catId` = ?";

    public final static String NOTE_DELETE_BY_NOTELOCALID =
            "DELETE FROM `Note` WHERE `noteLocalId` = ? ";

    public final static String NOTE_DELETE_BY_NOTEID =
            "DELETE FROM `Note` WHERE `noteId` = ? ";

    public final static String NOTE_DROP_TABLE =
            "DROP TABLE `Note`";

    //------------------------------------------------------------------------------
// `Attachment` Table
    public final static String ATT_CREATE_TABLE =
            "CREATE TABLE `Attachment`(" +
                    "`attLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
                    ",`attName` TEXT(100) NOT NULL" +
                    ",`type` INTEGER NOT NULL" +
                    ",`path` TEXT(100) NOT NULL" +
                    ",`noteLocalId` LONG NOT NULL" +
                    ",`size` INTEGER NOT NULL" +
                    ",`syncState` INTEGER NOT NULL" +
                    ",`digest` TEXT(32) NOT NULL" +
                    ",`attId` LONG NOT NULL" +
                    ",`width` INTEGER NOT NULL" +
                    ",`height` INTEGER NOT NULL" +
                    ")";

    public final static String ATT_INSERT =
            "INSERT INTO `Attachment` " +
                    "(`attName`,`type`,`path`,`noteLocalId`,`size`," +
                    "`syncState`,`digest`,`attId`,`width`,`height`) " +
                    "VALUES (?,?,?,?,?," +
                    "?,?,?,?,?)";

    public final static String ATT_UPDATE =
            "UPDATE `Attachment` " +
                    "SET `attName` = ?, `type` = ?, `path` = ?, `noteLocalId` = ?, `size` = ?, `digest` = ?, `attId` = ?, `width` = ?, `height` = ? " +
                    "WHERE `attId` = ?";

    public final static String ATT_UPDATE_ATTLOCALID =
            "UPDATE `Attachment` " +
                    "SET `attName` = ?, `type` = ?, `path` = ?, `noteLocalId` = ?, `size` = ?, `syncState` = ?, `digest` = ?, `attId` = ?, `width` = ?, `height` = ? " +
                    "WHERE `attLocalId` = ?";

    public final static String ATT_COUNT =
            "SELECT COUNT(`attLocalId`) FROM `Attachment` " +
                    "WHERE `noteLocalId`= ?";

    public final static String ATT_SELECT_BYID =
            "SELECT * FROM `Attachment` WHERE `attLocalId`= ?";

    public final static String ATT_SELECT_BY_NOTEID =
            "SELECT * FROM `Attachment` WHERE `noteLocalId`= ?";

    public final static String ATT_SELECT_WH =
            "SELECT * FROM `Attachment` " +
                    "WHERE `width` > ? AND `height` > ? AND `noteLocalId`= ?";

    public final static String ATT_SYNCSTATE =
            "SELECT SUM(`syncState`) FROM `Attachment` WHERE `noteLocalId` = ?";

    public final static String ATT_UPDATE_SYNCSTATE =
            "UPDATE `Attachment` SET `syncState`=? WHERE `attLocalId`=?";

    public final static String ATT_UPDATE_SYNCSTATE_ATTID =
            "UPDATE `Attachment` SET `syncState`=?, `attId`=? WHERE `attLocalId`=?";

    public final static String ATT_PATH =
            "UPDATE `Attachment` " +
                    "SET `path` = ?" +
                    "WHERE `attLocalId` = ?";

    public final static String ATT_SET_ID =
            "UPDATE `Attachment` SET `attId` = ? WHERE `attLocalId` = ?";

    public final static String ATT_CHECK_ID =
            "SELECT `attLocalId`, `syncState` FROM `Attachment` WHERE `attId` = ?";

    public final static String ATT_FOR_ADD =
            "SELECT `attLocalId` FROM `Attachment` " +
                    "WHERE `noteLocalId` = ? AND `syncState` = 1";

    public final static String ATT_FORUPDATE =
            "SELECT `attLocalId`, `attName`, `type`, `size`, " +
                    "`digest`, `attId` " +
                    "FROM `Attachment` " +
                    "WHERE `noteLocalId` = ? AND `syncState` = 1";

    public final static String ATT_SET_STATUS =
            "UPDATE `Attachment` SET `path` = ?, `syncState` = ? " +
                    "WHERE `attId` = ?";

    public final static String ATT_SET_DOWNLOADED =
            "UPDATE `Attachment` SET `path` = ?, " +
                    "`width` = ?, `height` = ?, `syncState` = ? " +
                    "WHERE `attId` = ?";

    public final static String ATT_IN_NOTE =
            "SELECT `attLocalId`, `attId`, `type` " +
                    "FROM `Attachment` WHERE `noteLocalId` = ?";

    public final static String ATT_CLEAR =
            "DELETE FROM `Attachment` WHERE " +
                    "`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userId`=?)";

    public final static String ATT_SET_TRASH =
            "UPDATE `Attachment` SET `trash` = ? WHERE `attLocalId` = ?";

    public final static String ATT_SET_TRASH_BYNOTE =
            "UPDATE `Attachment` SET `trash` = ? WHERE `noteLocalId` = ?";

    public final static String ATT_SET_TRASH_SYNCSTATE =
            "UPDATE `Attachment` SET `trash` = ?, `syncState` = ? WHERE `attLocalId` = ?";

    public final static String ATT_DELETE_NOTE =
            "DELETE FROM `Attachment` " +
                    "WHERE `noteLocalId` = ?";

    public final static String ATT_DELETE_ATT_ID =
            "DELETE FROM `Attachment` " +
                    "WHERE `attId` = ?";

    public final static String ATT_DELETE_ATTLOCALID =
            "DELETE FROM `Attachment` " +
                    "WHERE `attLocalId` = ?";

    public final static String ATT_DROP_TABLE =
            "DROP TABLE `Attachment`";

    //老数据库的===========================================
    public final static String NOTE_GET_OLD_DB =
            "SELECT `noteLocalId`, `title`, `content`, `createTime`, `lastUpdate`, `lbsLongitude`, `lbsLatitude`, `lbsRadius`, `lbsAddress` " +
                    "FROM `Note`" +
                    "WHERE `trash` = 0 AND `noteId` == -1 AND `userLocalId` = ? ";

    public final static String USER_CHECK_OLD_DB_USERID =
            "SELECT `userLocalId` FROM `User` WHERE `userId`=?";

    public final static String DLDDB_USERNAME_SELECT_BY_ID =
            "SELECT `username` FROM `User` WHERE `userId`=?";

    public final static String DLDDB_USERNAME_SELECT_ALL =
            "SELECT `username` FROM `User` ";

    public final static String ATT_SELECT_BY_NOTEID_BY_OLD_DB =
            "SELECT `attLocalId`, `attName`, `type`, `path` FROM `Attachment` WHERE `noteLocalId`= ?";
}
