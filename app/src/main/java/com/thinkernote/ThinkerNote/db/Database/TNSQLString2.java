package com.thinkernote.ThinkerNote.db.Database;

public class TNSQLString2 {

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
		"`userLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`username` TEXT(50) NOT NULL" +
		",`password` TEXT(32) NOT NULL" +
		",`userEmail` TEXT(100) NOT NULL" +
		",`status` INTEGER NOT NULL" +
		",`nickname` TEXT(50) NOT NULL" +
		",`defaultCatLocalId` LONG NOT NULL" +
		",`userId` LONG NOT NULL" +
		",`syncState` INTEGER NOT NULL" +
		",`revision` LONG NOT NULL" +
		",`syncRevision` LONG NOT NULL" +
		",`emailVerify` INTEGER NOT NULL" +
		",`totalSpace` LONG NOT NULL" +
		",`usedSpace` LONG NOT NULL" +
		",`contribution` INTEGER NOT NULL" +
		",`conRank` INTEGER NOT NULL" +
		",`catSyncRevision` LONG NOT NULL DEFAULT 0" +
		",`tagSyncRevision` LONG NOT NULL DEFAULT 0" +
		",`projectSyncRevision` LONG NOT NULL DEFAULT 0" +
		",`preCode` TEXT(32) NOT NULL DEFAULT ''" +
		",`inviteName` TEXT(50) NOT NULL DEFAULT ''" +
		")";

	public final static String USER_INSERT =
		"INSERT INTO `User` " +
		"(`username`,`password`,`userEmail`,`nickname`,`defaultCatLocalId`," +
		"`userId`, `syncState`, `revision`, `syncRevision`, `status`, " +
		"`emailVerify`, `totalSpace`, `usedSpace`, `contribution`, `conRank`, `preCode`) " +
		"VALUES (?,?,?,?,?," +
		"?,?,?,?,?," +
		"?,?,?,?,?,?)";
	
	public final static String USER_UPDATE_REG = 
		"UPDATE `User` SET `username`=?, `password`=?, `userEmail`=?, " +
		"`nickname`=?, `userId`=? WHERE `userLocalId`=?";
	
	public final static String USER_CHECK_USERID =
		"SELECT `userLocalId` FROM `User` WHERE `userId`=?";

	public final static String USER_CHECK_USERNAME =
		"SELECT `userLocalId` FROM `User` WHERE `username`=? OR `userEmail`=?";

	public final static String USER_CHECK_LOGIN =
		"SELECT `userLocalId`, `userId` FROM `User` WHERE (`username`=? OR `userEmail`=?) " +
		"AND `password`=?";
	
	public final static String USER_UPDATE_NAME = 
		"UPDATE `User` SET `username`=?, `nickname` = ? WHERE `userLocalId`=?";
	
	public final static String USER_UPDATE_INVITENAME =
		"UPDATE `User` SET `inviteName`=? WHERE `userLocalId`=?";

	public final static String USER_UPDATE_PWD =
		"UPDATE `User` SET `password`=? WHERE `userLocalId`=?";
	
	public final static String USER_UPDATE_PWD_AND_PRECODE =
		"UPDATE `User` SET `password`=?, `preCode`=? WHERE `userLocalId`=?";
	
	public final static String USER_UPDATE_NAME_AND_PWD = 
		"UPDATE `User` SET `username`=?, `nickname`=?, `password`=?, `preCode`=? WHERE `userLocalId`=?";

	public final static String USER_UPDATE_EMAIL =
		"UPDATE `User` SET `userEmail`=?, `emailVerify`=-1 WHERE `userLocalId`=?";

	public final static String USER_UPDATE_DEFAULTCAT =
		"UPDATE `User` SET `defaultCatLocalId`=? WHERE `userLocalId`=?";

	public final static String USER_UPDATE_SYNCSTATE =
		"UPDATE `User` SET `syncState`=? WHERE `userLocalId`=?";
	
	public final static String USER_SET_REVISION =
		"UPDATE `User` SET `revision`=? WHERE `userLocalId`=?";

	public final static String USER_SELECT_BY_ID =
		"SELECT `username`, `password`, `userEmail`, `status`, `nickname`, " +
		"`defaultCatLocalId`, `userId`, `syncState`, `revision`, `syncRevision`, " +
		"`emailVerify`, `totalSpace`, `usedSpace`, `contribution`, `conRank`, `preCode`, `inviteName` " +
		"FROM `User` WHERE `userLocalId`=?";
	
	public final static String USER_SELECT_ALL =
		"SELECT `username` FROM `User` ";

	public final static String USER_SELECT_SYNC_REVISION =
		"SELECT `syncRevision`, `catSyncRevision`, `tagSyncRevision` , `projectSyncRevision`" +
		"FROM `User` WHERE `userLocalId`=?";

	public final static String USER_UPDATE =
		"UPDATE `User` " +
		"SET `username` = ?, `userEmail`=?, `nickname`=?, `defaultCatLocalId`=?, " +
		"`syncState`=?, `revision`=?, `status`=?, `emailVerify`=?, " +
		"`totalSpace`=?, `usedSpace`=?, `contribution`=?, `conRank`=?, `inviteName`=? " +
		"WHERE `userLocalId` = ?";

	public final static String USER_SET_SYNCREVISION =
		"UPDATE `User` SET `syncRevision` = ? " +
		"WHERE `userLocalId` = ?";

	public final static String USER_SET_CATSYNCREVISION =
		"UPDATE `User` SET `catSyncRevision` = ? " +
		"WHERE `userLocalId` = ?";

	public final static String USER_SET_TAGSYNCREVISION =
		"UPDATE `User` SET `tagSyncRevision` = ? " +
		"WHERE `userLocalId` = ?";
	
	public final static String USER_SET_PROJECT_SYNCREVISION = 
		"UPDATE `User` SET `projectSyncRevision` = ? " +
		"WHERE `userLocalId` = ?";	

	public final static String USER_CLEAR =
		"DELETE FROM `User` WHERE `userLocalId` = ?";
	
	public final static String USER_DROP_TABLE =
		"DROP TABLE `User`";

//------------------------------------------------------------------------------
// `Category` Table
	public final static String CAT_CREATE_TABLE =
		"CREATE TABLE `Category`(" +
		"`catLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`catName` TEXT(100) NOT NULL" +
		",`userLocalId` LONG NOT NULL" +
		",`trash` INTEGER NOT NULL" +
		",`syncState` INTEGER NOT NULL" +
		",`catId` LONG NOT NULL" +
		",`revision` LONG NOT NULL" +
		",`deep` INTEGER NOT NULL DEFAULT 0" +
		",`pCatLocalId` LONG NOT NULL DEFAULT 0" +
		",`isLeaf` INTEGER NOT NULL DEFAULT 1" +
		",`projectLocalId` LONG NOT NULL DEFAULT 0" +
		",`readPriv` INTEGER NOT NULL DEFAULT 1" +
		",`writePriv` INTEGER NOT NULL DEFAULT 1" +
		",`createPriv` INTEGER NOT NULL DEFAULT 1" +
		",`deletePriv` INTEGER NOT NULL DEFAULT 1" +
		",`managePriv` INTEGER NOT NULL DEFAULT 1" +
		",`isNew` INTEGERN NOT NULL DEFAULT 0" +
		",`createTime` LONG NOT NULL DEFAULT 0" +
		",`lastUpdateTime` LONG NOT NULL DEFAULT 0" +
		")";
	
	public final static String CAT_INSERT =
		"INSERT INTO `Category` " +
		"(`catName`,`userLocalId`,`trash`,`syncState`,`catId`,`revision`, " +
		"`deep`, `pCatLocalId`, `isLeaf`, `projectLocalId`, `readPriv`, `writePriv`, " +
		"`createPriv`, `deletePriv`, `managePriv`, `isNew`, `createTime`, `lastUpdateTime`)" +
		"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public final static String CAT_UPDATE =
		"UPDATE `Category` " +
		"SET `catName` = ?, `trash` = ?, `syncState` = ?, " +
		"`catId` = ?, `revision` = ?, `deep` = ?, `pCatLocalId` = ?, " +
		"`isLeaf` = ?, `readPriv` = ?, `writePriv` = ?, " +
		"`createPriv` = ?, `deletePriv` = ?, `managePriv` = ?, " +
		"`createTime` = ?, `lastUpdateTime` = ? " +
		"WHERE `catLocalId` = ?";
	
	public final static String CAT_SELECT = 
		"SELECT `catLocalId`, `trash`, `syncState`, `catId`, `pCatLocalId`" +
		"FROM `Category`" +
		"WHERE `catLocalId` = ?";

	public final static String CAT_CHECK_CATID =
		"SELECT `catLocalId` FROM `Category` " +
		"WHERE `userLocalId` = ? AND `catId` = ? AND `trash`=0";
	
	public final static String CAT_SELECT_FORID = 
		"SELECT `catLocalId`, `trash`, `syncState`, `catId`, `pCatLocalId` " +
		"FROM `Category` " +
		"WHERE `userLocalId` = ? AND `catId` = ? ";
	
	public final static String CAT_SELECT_FORID1 = 
		"SELECT `catLocalId`, `trash`, `syncState`, `catId`, `pCatLocalId` " +
		"FROM `Category` " +
		"WHERE `catId` = ? ";
	
	public final static String CAT_SELECT_CHILD = 
		"SELECT `catLocalId` FROM `Category` " +
		"WHERE `userLocalId` = ? AND `pCatLocalId` = ? AND `trash` = 0";
	
	public final static String CAT_SELECT_CHILD_FOR_DELETE = 
		"SELECT `catLocalId`, `deletePriv` " +
		"FROM `Category` " +
		"WHERE `userLocalId` = ? AND `pCatLocalId` = ? AND `trash` = 0";
	
	public final static String CAT_CHECK_CATID_CONFLICT =
		"SELECT `catLocalId` FROM `Category` " +
		"WHERE `catId` = ? AND `trash`= 1 AND `projectLocalId` = ? AND `userLocalId` = ?";
	
	public final static String CAT_CHACK_NEW =
		"SELECT `catId` FROM `Category` " +
		"WHERE `projectLocalId` = ? AND `trash` = 0 AND `isNew` = 1";
	
	public final static String CAT_SET_ISNEW =
		"UPDATE `Category` " +
		"SET `isNew` = 0 " +
		"WHERE `catLocalId` = ?";

	public final static String CAT_SELECT_ALL_PROJECT =
		"SELECT `Category`.`catLocalId`, `Category`.`catName`, " +
		"`NoteCount`.`noteCounts`, `NewNoteCount`.`newNoteCounts`, " +
		"`Category`.`deep`, `Category`.`pCatLocalId`, `Category`.`isLeaf`, " +
		"`Category`.`projectLocalId`, `Category`.`readPriv`, `Category`.`writePriv`, " +
		"`Category`.`createPriv`, `Category`.`deletePriv`, `Category`.`managePriv`, " +
		"`Category`.`catId`, `Category`.`createTime`, `Category`.`lastUpdateTime` " +
		"FROM `Category` " +
		"LEFT JOIN ( " +
			"SELECT `catLocalId`, COUNT(`noteLocalId`) AS `noteCounts` " +
			"FROM `Note` " +
			"WHERE `userLocalId` = ? AND `projectLocalId` = ? AND `trash` = 0 " +
			"GROUP BY `catLocalId` " +
		") `NoteCount` " +
		"ON `Category`.`catLocalId` = `NoteCount`.`catLocalId` " +
		"LEFT JOIN ( " +
			"SELECT `catLocalId`, COUNT(`noteLocalId`) AS `newNoteCounts` FROM ( " +
				"SELECT `catLocalId`, `noteLocalId` FROM `Note` " +
				"WHERE `userLocalId` = ? AND `projectLocalId` = ? " +
				"AND `trash` = 0 AND `syncState` = 1 " +
			"UNION " +
				"SELECT `Note`.`catLocalId`, `Note`.`noteLocalId` " +
				"FROM `Note`, `NoteTag` " +
				"WHERE `Note`.`userLocalId` = ? AND `Note`.`projectLocalId` = ? " +
				"AND `Note`.`noteLocalId` = `NoteTag`.`noteLocalId` " +
				"AND `Note`.`trash` = 0 AND `NoteTag`.`syncState` = 1 " +
			"UNION " +
				"SELECT `Note`.`catLocalId`, `Note`.`noteLocalId` " +
				"FROM `Note`, `Attachment` " +
				"WHERE `Note`.`userLocalId` = ? AND `Note`.`projectLocalId` = ? " +
				"AND `Note`.`noteLocalId` = `Attachment`.`noteLocalId` " +
				"AND `Note`.`trash` = 0 AND " + 
				"(`Attachment`.`syncState` = 1 " +
				"OR (`Attachment`.`uploadFlag` = -1 AND `Attachment`.`trash` = 0)) " +
			")T GROUP BY `catLocalId` " +
		") `NewNoteCount` " +
		"ON `Category`.`catLocalId` = `NewNoteCount`.`catLocalId` " +
		"WHERE `Category`.`userLocalId` = ? AND `Category`.`projectLocalId` = ? " +
		"AND `Category`.`trash` = 0 " +
		"ORDER BY `NoteCount`.`noteCounts` DESC";	

	public final static String CAT_SELECT_ONE =
			"SELECT `Category`.`catLocalId`, `Category`.`catName`, " +
			"`NoteCount`.`noteCounts`, `NewNoteCount`.`newNoteCounts`, " +
			"`Category`.`deep`, `Category`.`pCatLocalId`, `Category`.`isLeaf`, " +
			"`Category`.`projectLocalId`, `Category`.`readPriv`, `Category`.`writePriv`, " +
			"`Category`.`createPriv`, `Category`.`deletePriv`, `Category`.`managePriv` " +
			"FROM `Category` " +
			"LEFT JOIN ( " +
				"SELECT `catLocalId`, COUNT(`noteLocalId`) AS `noteCounts` " +
				"FROM `Note` " +
				"WHERE `catLocalId` = ? AND `trash` = 0 " +
				"GROUP BY `catLocalId` " +
			") `NoteCount` " +
			"ON `Category`.`catLocalId` = `NoteCount`.`catLocalId` " +
			"LEFT JOIN ( " +
				"SELECT `catLocalId`, COUNT(`noteLocalId`) AS `newNoteCounts` FROM ( " +
					"SELECT `catLocalId`, `noteLocalId` FROM `Note` " +
					"WHERE `catLocalId` = ? " +
					"AND `trash` = 0 AND `syncState` = 1 " +
				"UNION " +
					"SELECT `Note`.`catLocalId`, `Note`.`noteLocalId` " +
					"FROM `Note`, `NoteTag` " +
					"WHERE `Note`.`catLocalId` = ? " +
					"AND `Note`.`noteLocalId` = `NoteTag`.`noteLocalId` " +
					"AND `Note`.`trash` = 0 AND `NoteTag`.`syncState` = 1 " +
				"UNION " +
					"SELECT `Note`.`catLocalId`, `Note`.`noteLocalId` " +
					"FROM `Note`, `Attachment` " +
					"WHERE `Note`.`catLocalId` = ? " +
					"AND `Note`.`noteLocalId` = `Attachment`.`noteLocalId` " +
					"AND `Note`.`trash` = 0 AND " + 
					"(`Attachment`.`syncState` = 1 " +
					"OR (`Attachment`.`uploadFlag` = -1 AND `Attachment`.`trash` = 0)) " +
				")T GROUP BY `catLocalId` " +
			") `NewNoteCount` " +
			"ON `Category`.`catLocalId` = `NewNoteCount`.`catLocalId` " +
			"WHERE `Category`.`catLocalId` = ? " +
			"AND `Category`.`trash` = 0 " +
			"ORDER BY `NoteCount`.`noteCounts` DESC";	

	public final static String CAT_SELECT_ALL =
		"SELECT `Category`.`catLocalId`, `Category`.`catName`, " +
		"`NoteCount`.`noteCounts`, `NewNoteCount`.`newNoteCounts`, " +
		"`Category`.`deep`, `Category`.`pCatLocalId`, `Category`.`isLeaf`" +
		"FROM `Category` " +
		"LEFT JOIN ( " +
			"SELECT `catLocalId`, COUNT(`noteLocalId`) AS `noteCounts` " +
			"FROM `Note` WHERE `userLocalId` = ? AND `trash` = 0 " +
			"GROUP BY `catLocalId` " +
		") `NoteCount` ON `Category`.`catLocalId` = `NoteCount`.`catLocalId` " +
		"LEFT JOIN ( " +
				"SELECT `catLocalId`, COUNT(`noteLocalId`) AS `newNoteCounts` FROM ( " +
				"SELECT `catLocalId`, `noteLocalId` FROM `Note` WHERE `userLocalId` = ? " +
				"AND `trash` = 0 AND `syncState` = 1 " +
			"UNION " +
				"SELECT `Note`.`catLocalId`, `Note`.`noteLocalId` FROM `Note`, `NoteTag` " +
				"WHERE `Note`.`userLocalId` = ? AND `Note`.`noteLocalId` = `NoteTag`.`noteLocalId` " +
				"AND `Note`.`trash` = 0 AND `NoteTag`.`syncState` = 1 " +
			"UNION " +
				"SELECT `Note`.`catLocalId`, `Note`.`noteLocalId` FROM `Note`, `Attachment` " +
				"WHERE `Note`.`userLocalId` = ? AND `Note`.`noteLocalId` = `Attachment`.`noteLocalId` " +
				"AND `Note`.`trash` = 0 AND " + 
				"(`Attachment`.`syncState` = 1 OR (`Attachment`.`uploadFlag` = -1 AND `Attachment`.`trash` = 0)) " +
			")T GROUP BY `catLocalId` " +
		") `NewNoteCount` ON `Category`.`catLocalId` = `NewNoteCount`.`catLocalId` " +
		"WHERE `Category`.`userLocalId` = ? AND `Category`.`trash` = 0 " +
		"ORDER BY `NoteCount`.`noteCounts` DESC";

	public final static String CAT_NAMECHECK =
		"SELECT `catLocalId`, `trash` FROM `Category` " +
		"WHERE `catName` = ? AND `userLocalId` = ? AND `projectLocalId` = ? " +
		"AND `trash` = 0 AND `pCatLocalId` = ?";

	public final static String CAT_UPDATE_TRASH =
		"UPDATE `Category` SET `trash` = ? " +
		"WHERE `catLocalId` = ?";
	
	public final static String CAT_DELETE = 
		"UPDATE `Category` SET `trash` = 1, `syncState` = 1, `lastUpdateTime` = ? " +
		"WHERE `catLocalId` = ?";
	
	public final static String CAT_MOVE_USER =
		"UPDATE `Category` SET `userLocalId` = ? WHERE `userLocalId` = ? ";
	
	public final static String CAT_SET_TRASH_FOR_PRIV =
		"UPDATE `Category` SET `trash` = ?, `deletePriv` = 0 " +
		"WHERE `catLocalId` = ?";	

	public final static String CAT_RENAME =
		"UPDATE `Category` " +
		"SET `catName` = ?, `lastUpdateTime` = ? " +
		"WHERE `catLocalId` = ?";

	public final static String CAT_SET_PARENT =
		"UPDATE `Category` " +
		"SET `deep` = ?, `pCatLocalId` = ?, `lastUpdateTime` = ? " +
		"WHERE `catLocalId` = ? ";

	public final static String CAT_UPDATE_SYNCSTATE =
		"UPDATE `Category` SET `syncState`=? WHERE `catLocalId`=?";

	public final static String CAT_FOR_ADD =
		"SELECT `catLocalId`, `catName`, `deep`, `pCatLocalId`, " +
		"`isLeaf`, `projectLocalId`, `createTime`, `lastUpdateTime` " +
		"FROM `Category` " +
		"WHERE `userLocalId` = ? AND `projectLocalId` = ? AND `syncState` = 1 " + 
		"AND `catId` = -1 AND `trash` = 0 " +
		"ORDER BY `deep` ASC";

	public final static String CAT_SET_ID =
		"UPDATE `Category` " +
		"SET `catId` = ?, `revision` = ? " +
		"WHERE `catLocalId` = ? ";
	
	public final static String CAT_SET_PRIV_CREATE =
		"UPDATE `Category` " +
		"SET `createPriv` = ? " +
		"WHERE `catLocalId` = ? ";
	
	public final static String CAT_SET_PRIV_DELETE = 
		"UPDATE `Category` " +
		"SET `deletePriv` = ? " +
		"WHERE `catLocalId` = ? ";
	
	public final static String CAT_SET_TRASH_AND_DELETEPRIV =
		"UPDATE `Category` " +
		"SET `deletePriv` = ?, `trash` = ? " +
		"WHERE `catLocalId` = ? ";	
	
	public final static String CAT_SET_PRIV_WRITE =
		"UPDATE `Category` " +
		"SET `writePriv` = ? " +
		"WHERE `catLocalId` = ? ";
	
	public final static String CAT_SET_PRIV_READ =
		"UPDATE `Category` " +
		"SET `readPriv` = ? " +
		"WHERE `catLocalId` = ? ";

	public final static String CAT_SET_INFO =
		"UPDATE `Category` " +
		"SET `catId` = ?, `revision` = ?, `deep` = ?, `pCatLocalId`=?, `isLeaf`=? " +
		"WHERE `catLocalId` = ? ";

	public final static String CAT_FOR_UPDATE =
		"SELECT `catLocalId`, `catName`, `trash`, `catId`, `revision`, `deep`, `pCatLocalId`, " +
		"`createTime`, `lastUpdateTime` " +
		"FROM `Category` " +
		"WHERE `userLocalId` = ? AND `projectLocalId` = ? " +
		"AND `syncState` = 1 AND `catId` != -1 " +
		"ORDER BY `deep` DESC";
	
	public final static String CAT_SET_REVISION =
		"UPDATE `Category` SET `revision` = ? WHERE `catLocalId` = ?";

	public final static String CAT_MINREVISION =
		"SELECT MIN(`revision`) FROM `Category` WHERE `userLocalId` = ? AND `syncState` = 0";

	public final static String CAT_SET_OTHERS_REVISION =
		"UPDATE `Category` SET `revision` = ? " +
		"WHERE `userLocalId` = ? AND `syncState` = 0 ";

	public final static String CAT_SELECT_BY_ID =
		"SELECT `catLocalId`, `catName`, `userLocalId`, `trash`, `syncState`, " +
		"`catId`, `revision`, `deep`, `pCatLocalId`, `isLeaf`, `projectLocalId` " +
		"FROM `Category` " +
		"WHERE `catLocalId` = ? ";
	
	public final static String CAT_GETLOCALID_BYATT = 
		"SELECT `Note.catLocalId` " +
		"FROM `Note`, `Attachment` " +
		"WHERE `Note`.`noteLocalId` = `Attachment`.`noteLocalId`" +
		"AND `Attachment`.`attLocalId` = ?";	

	public final static String CAT_CLEAR =
		"DELETE FROM `Category` WHERE `userLocalId` = ?";

	public final static String CAT_DELETE_PROJECT =
		"DELETE FROM `Category` WHERE `userLocalId` = ? AND `projectLocalId` = ?";

	public final static String CAT_DELETE_CAT =
		"DELETE FROM `Category` WHERE `catLocalId` = ?";
	
	public final static String CAT_DROP_TABLE =
		"DROP TABLE `Category`";

//------------------------------------------------------------------------------
// `Tag` Table
	public final static String TAG_CREATE_TABLE =
		"CREATE TABLE `Tag`(" +
		"`tagLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`tagName` TEXT(50) NOT NULL" +
		",`userLocalId` LONG NOT NULL" +
		",`trash` INTEGER NOT NULL" +
		",`syncState` INTEGER NOT NULL" +
		",`tagId` LONG NOT NULL" +
		",`revision` LONG NOT NULL" +
		",`strIndex` TEXT(8) NOT NULL DEFAULT ''" + 
		")";
	
	public final static String TAG_INSERT =
		"INSERT INTO `Tag` " +
		"(`tagName`, `userLocalId`, `trash`, `syncState`, `tagId`, `revision`, `strIndex`) " +
		"VALUES (?, ?, ?, ?, ?, ?)";

	public final static String TAG_UPDATE =
		"UPDATE `Tag` " +
		"SET `tagName` = ?, `trash` = ?, `syncState` = ?, " +
		"`tagId` = ?, `revision` = ?, `strIndex` = ? " +
		"WHERE `tagLocalId` = ?";
	
	public final static String TAG_GET = 
		"SELECT `tagName`, `trash`, `syncState`, `tagId`, `revision` " +
		"FROM `Tag` " +
		"WHERE `tagLocalId` = ?";
	
	public final static String TAG_GET_ALL = 
		"SELECT `tagLocalId`, `tagName` FROM `Tag` ";
	
	public final static String TAG_UPDATA_INDEX = 	
		"UPDATE `Tag` " +
		"SET `strIndex` = ? " +
		"WHERE `tagLocalId` = ? ";

	public final static String TAG_SELECT_ALL =
		"SELECT `Tag`.`tagLocalId`, `Tag`.`tagName`, `NoteCount`.`noteCounts`, `Tag`.`strIndex` " +
		"FROM `Tag` " +
		"LEFT JOIN ( " +
			"SELECT `NoteTag`.`tagLocalId` AS `tagLocalId`, COUNT(`NoteTag`.`noteLocalId`) AS `noteCounts` " +
			"FROM `NoteTag`, `Note` " +
			"WHERE `NoteTag`.`trash` = 0 AND `Note`.`trash` = 0 " +
			"AND `NoteTag`.`noteLocalId` = `Note`.`noteLocalId` " +
			"GROUP BY `NoteTag`.`tagLocalId` " +
		") `NoteCount` ON `Tag`.`tagLocalId` = `NoteCount`.`tagLocalId` " +
		"WHERE `userLocalId` = ? AND `trash` = 0 " +
		"ORDER BY `strIndex` ASC";

	public final static String TAG_NAMECHECK =
		"SELECT `tagLocalId`, `trash` FROM `Tag` " +
		"WHERE `tagName` = ? AND `userLocalId` = ?";
	
	public final static String TAG_UPDATE_TRASH =
		"UPDATE `Tag` SET `trash` = ? " +
		"WHERE `tagLocalId` = ?";

	public final static String TAG_RENAME =
		"UPDATE `Tag` " +
		"SET `tagName` = ?, `strIndex` = ? " +
		"WHERE `tagLocalId` = ?";
	
	public final static String TAG_MOVE_USER =
		"UPDATE `Tag` SET `userLocalId` = ? WHERE `userLocalId` = ?";

	public final static String TAG_UPDATE_SYNCSTATE =
		"UPDATE `Tag` SET `syncState`=? WHERE `tagLocalId`=?";
	
	public final static String TAG_FOR_ADD =
		"SELECT `tagLocalId`, `tagName` FROM `Tag` " +
		"WHERE `userLocalId` = ? AND `syncState` = 1 AND `trash` = 0 AND `tagId` = -1";
	
	public final static String TAG_FOR_UPDATE =
		"SELECT `tagLocalId`, `tagName`, `trash`, `tagId`, `revision` FROM `Tag` " +
		"WHERE `userLocalId` = ? AND `syncState` = 1 AND `tagId` != -1";

	public final static String TAG_MINREVISION =
		"SELECT MIN(`revision`) FROM `Tag` WHERE `userLocalId` = ? AND `syncState` = 0";

	public final static String TAG_SET_ID =
		"UPDATE `Tag` SET `tagId` = ?, `revision` = ? " +
		"WHERE `tagLocalId` = ? ";

	public final static String TAG_SET_REVISION =
		"UPDATE `Tag` SET `revision` = ? WHERE `tagLocalId` = ?";
	
	public final static String TAG_SET_OTHERS_REVISION =
		"UPDATE `Tag` SET `revision` = ? " +
		"WHERE `userLocalId` = ? AND `syncState` = 0 ";
	
	public final static String TAG_CHECK_ID = 
		"SELECT `tagLocalId`, `syncState` FROM `Tag` WHERE `tagId` = ? AND `trash` = 0";

	public final static String TAG_CHECK_ID_WITHOUT_TRASH = 
		"SELECT `tagLocalId`, `syncState`, `trash` FROM `Tag` WHERE `tagId` = ?";

	public final static String TAG_CLEAR =
		"DELETE FROM `Tag` WHERE `userLocalId` = ?";
	
	public final static String TAG_REAL_DELETE = 
		"DELETE FROM `Tag` WHERE `tagLocalId` = ?";
	
	public final static String TAG_DROP_TABLE =
		"DROP TABLE `Tag`";

//------------------------------------------------------------------------------
// `Note` Table
	public final static String NOTE_CREATE_TABLE =
		"CREATE TABLE `Note`(" +
		"`noteLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`title` TEXT(100) NOT NULL" +
		",`userLocalId` LONG NOT NULL" +
		",`catLocalId` LONG NOT NULL" +
		",`content` TEXT(102400) NOT NULL" +
		",`contentDigest` TEXT(32) NOT NULL" +
		",`trash` INTEGER NOT NULL" +
		",`client` TEXT(100) NOT NULL" +
		",`source` TEXT(1000) NOT NULL" +
		",`createTime` LONG NOT NULL" +
		",`lastAccess` LONG NOT NULL" +
		",`lastUpdate` LONG NOT NULL" +
		",`accessTimes` INTEGER NOT NULL" +
		",`syncState` INTEGER NOT NULL" +
		",`noteId` LONG NOT NULL" +
		",`revision` LONG NOT NULL" +
		",`share` INTEGER NOT NULL DEFAULT 0" +
		",`projectLocalId` LONG NOT NULL DEFAULT 0" +		
		",`readPriv` INTEGER NOT NULL DEFAULT 1" +
		",`writePriv` INTEGER NOT NULL DEFAULT 1" +
		",`deletePriv` INTEGER NOT NULL DEFAULT 1" +
		",`managePriv` INTEGER NOT NULL DEFAULT 1" +
		",`comCount` INTEGER NOT NULL DEFAULT 0" +
		",`comSyncRevision` LONG NOT NULL" +
		",`comLatestRevision` LONG NOT NULL" +
		",`allowComment` INTEGER NOT NULL DEFAULT 0" +
		",`shortContent` TEXT(200) NOT NULL DEFAULT ''" +
		",`pingYinIndex` TEXT(32) NOT NULL DEFAULT ''" +
		",`creatorUserId` LONG NOT NULL DEFAULT 0" +
		",`lbsLongitude` INTEGER NOT NULL DEFAULT 0" +
		",`lbsLatitude` INTEGER NOT NULL DEFAULT 0" +
		",`lbsRadius` INTEGER NOT NULL DEFAULT 0" +
		",`lbsAddress` TEXT(100) NOT NULL DEFAULT ''" +
		",`nickName` TEXT(50) NOT NULL DEFAULT ''" +
		",`sourceShowText` TEXT(100) NOT NULL DEFAULT ''" +
		",`sourceHyperlink` TEXT(1000) NOT NULL DEFAULT ''" +
		",`thumbnailId` INTEGER NOT NULL DEFAULT 0" +			//0 未下载 -1没有缩略图  >0缩略图的源附件id
		",`sharePassword` TEXT(32) NOT NULL DEFAULT ''" + 
		")";
	
	public final static String NOTE_COUNT_TRASH =
		"SELECT count(`noteLocalId`) " +
		"FROM `Note` " +
		"WHERE `trash` = ? AND `userLocalId` = ? AND `projectLocalId` = ? ";
	
	public final static String NOTE_GET_ALL = 
		"SELECT `noteLocalId`, `content` , `title` " +
		"FROM `Note`";
	
	public final static String NOTE_UPDATE_SHORTCONTENT =
		"UPDATE `Note` " +
		"SET `shortContent` = ? " +
		"WHERE `noteLocalId` = ? ";
	
	public final static String NOTE_UPDATE_PINGYININDEX =
		"UPDATE `Note` " +
		"SET `pingYinIndex` = ? " +
		"WHERE `noteLocalId` = ? ";

	public final static String NOTE_COUNT_BYCAT =
		"SELECT count(`noteLocalId`) FROM `Note` " +
		"WHERE `trash` = 0 AND `catLocalId` = ?";
	
	public final static String NOTE_GET_CATID = 
		"SELECT `catLocalId` FROM `Note` " +
		"WHERE `noteLocalId` = ?";	
	
	public final static String NOTE_COUNT_BYTAG =
		"SELECT count(`Note`.`noteLocalId`) FROM `Note`, `NoteTag` " +
		"WHERE `NoteTag`.`trash` = 0 AND `NoteTag`.`tagLocalId` = ? " +
		"AND `Note`.`noteLocalId` = `NoteTag`.`noteLocalId` AND `Note`.`trash` = 0 ";

	public final static String NOTE_INSERT_SHARENOTE =
		"INSERT INTO `Note` " +
		"(`title`, `userLocalId`, `catLocalId`, `content`, `contentDigest`, `trash`, `client`, " +
		"`source`, `createTime`, `lastAccess`, `lastUpdate`, `accessTimes`, `syncState`, `noteId`, " +
		"`revision`, `share`, `projectLocalId`, `readPriv`, `writePriv`, `deletePriv`, `managePriv`, " +
		"`comCount`, `comSyncRevision`, `comLatestRevision`, `allowComment`, `shortContent`, `pingYinIndex`, `creatorUserId` " +
		"`lbsLongitude`, `lbsLatitude`, `lbsRadius`, `lbsAddress`, `nickName`, `sourceShowText`, `sourceHyperlink`, " +
		"`thumbnailId`, `sharePassword` )" +
		"VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ? ,?, ?, ?, " +
		"?, ?, ?, ? ,?, ?, ?, " +
		"?, ?, ?, ?, ?, ?, ?, " +
		"?, ? )";
	
	public final static String NOTE_SELECT_WITHOUT_NOTESHARE = 
		"SELECT `noteLocalId`, `thumbnailId` FROM `Note` " +
		"WHERE `share` = 1 AND `userLocalId` = -1 AND `noteId` NOT IN (SELECT `noteId` FROM `NoteShare`)";
	
	public final static String NOTE_INSERT =
		"INSERT INTO `Note` " +
		"(`title`, `userLocalId`, `catLocalId`, `content`, `contentDigest`, `trash`, `client`, " +
		"`source`, `createTime`, `lastAccess`, `lastUpdate`, `accessTimes`, `syncState`, `noteId`, " +
		"`revision`, `share`, `projectLocalId`, `readPriv`, `writePriv`, `deletePriv`, `managePriv`, " +
		"`comCount`, `comSyncRevision`, `comLatestRevision`, `allowComment`, `shortContent`, `pingYinIndex`, `creatorUserId` " +
		"`lbsLongitude`, `lbsLatitude`, `lbsRadius`, `lbsAddress`, `nickName`, `sourceShowText`, `sourceHyperlink` )" +
		"VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ?, ?, ?, ?, " +
		"?, ?, ?, ? ,?, ?, ?, " +
		"?, ?, ?, ? ,?, ?, ?, " +
		"?, ?, ?, ?, ?, ?, ?, ? )";

	public final static String NOTE_INSERT_BLANK =
		"INSERT INTO `Note` " +
		"(`title`, `userLocalId`, `catLocalId`, `content`, `contentDigest`, `trash`, `client`, " +
		"`source`, `createTime`, `lastAccess`, `lastUpdate`, `accessTimes`, `syncState`, `noteId`, " +
		"`revision`, `comSyncRevision`, `comLatestRevision`) VALUES " +
		"('', -1, -1, '', '', 0, '', " +
		"'', 0, 0, 0, 0, 0, -1, -1, -1, -1)";
	
	public final static String NOTE_UPDATE =
		"UPDATE `Note` " +
		"SET `title` = ?, `catLocalId` = ?, `content` = ?, `contentDigest` = ?, " +
		"`trash` = ?, `createTime` = ?, `lastAccess` = ?, `lastUpdate` = ?, `accessTimes` = ?, " +
		"`syncState` = ?, `share` = ?, " +
		"`readPriv` = ?, `writePriv` = ?, `deletePriv` = ?, `managePriv` = ?, " +
		"`comCount` = ?, `shortContent` = ?, `pingYinIndex` = ?, `allowComment` = ?, `sharePassword` = ? " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_UPDATE_SHARENOTE = 
		"UPDATE `Note` " +
		"SET `title` = ?, `shortContent` = ?, `thumbnailId` = ?, `nickName` = ?, " +
		"`sourceShowText` = ?, `sourceHyperlink` = ?, `comCount` = ?, `sharePassword` = ? " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_UPDATE_SHARENOTE_WITHOUT_SUMMARY = 
		"UPDATE `Note` " +
		"SET `title` = ?, `thumbnailId` = ?, `nickName` = ?, " +
		"`sourceShowText` = ?, `sourceHyperlink` = ?, `comCount` = ?, `sharePassword` = ? " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_UPDATE_THUMBNAILID = 
		"UPDATE `Note` SET `thumbnailId` = ? WHERE `noteId` = ?";
	
	public final static String NOTE_UPDATE_WITHOUT_CONTENT =
		"UPDATE `Note` " +
		"SET `title` = ?, `userLocalId` = ?, `creatorUserId` = ?, `catLocalId`= ?, `trash` = ?, " +
		"`client` = ?, `source` = ?, `createTime` = ?, `lastAccess` = ?, " + 
		"`lastUpdate` = ?, `accessTimes` = ?, `syncState` = ?, `noteId` = ?, " +
		"`revision` = ?, `share` = ?, `projectLocalId` = ?, " +
		"`readPriv` = ?, `writePriv` = ?, `deletePriv` = ?, `managePriv` = ?, " +
		"`allowComment` = ?, `pingYinIndex` = ?, `sharePassword` = ?, `shortContent` = ?, `thumbnailId` = ?  " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_UPDATE_WITHOUT_COMMENT =
		"UPDATE `Note` " +
		"SET `comLatestRevision` = ?, `comCount` = ?, `revision` = ? " +
		"WHERE `noteLocalId` = ?";	
	

	public final static String NOTE_CHECK_UPDATE_FOR_CAT =
		"SELECT count(`noteLocalId`) FROM `Note` " +
		"WHERE `catLocalId` = ? " +
		"AND `trash` == 0 AND `noteId` != -1 AND `syncState` != 0 " +
		"AND `userLocalId` = ? AND `projectLocalId` = ?";

	public final static String NOTE_UPDATE_CONTENT =
		"UPDATE `Note` SET `content` = ?, `contentDigest` = ?, `shortContent` = ? " +
		"WHERE `noteLocalId` = ?";

	public final static String NOTE_UPDATE_ALLCOMMENT = 
		"UPDATE `Note` SET `allowComment` = ? " +
		"WHERE `noteLocalId` = ?";	

	public final static String NOTE_SET_DIGEST =
		"UPDATE `Note` SET `contentDigest` = ? " +
		"WHERE `noteLocalId` = ?";

	public final static String NOTELIST_BYTRASH =
		"SELECT `noteLocalId`, `lastUpdate`, `createTime`, `pingYinIndex` " +
		"FROM `Note` " +
		"WHERE `userLocalId` = ? AND `projectLocalId` = ? AND `trash` = ? " +
		"ORDER BY `lastUpdate` DESC,`noteLocalId` DESC ";

	public final static String NOTELIST_BYCAT =
		"SELECT `noteLocalId`, `lastUpdate`, `createTime`, `pingYinIndex` " +
		"FROM `Note` WHERE `trash` = 0 AND `catLocalId` = ? " +
		"ORDER BY `lastUpdate` DESC,`noteLocalId` DESC ";

	public final static String NOTELIST_BYCATGROUP =
		"SELECT `Note`.`noteLocalId`, `Note`.`lastUpdate`, `Note`.`createTime`, `Note`.`pingYinIndex` " +
		"FROM `Note`, `Category` " + 
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Note`.`trash` = 0 AND `Category`.`pCatLocalId` = ? " +
		"ORDER BY `Note`.`lastUpdate` DESC, `Note`.`noteLocalId` DESC ";

	public final static String NOTELIST_BYTAG =
		"SELECT `Note`.`noteLocalId`, `Note`.`lastUpdate`, `Note`.`createTime`, `Note`.`pingYinIndex` " +
		"FROM `Note`, `NoteTag` " +
		"WHERE `NoteTag`.`trash` = 0 AND `NoteTag`.`tagLocalId` = ? " +
		"AND `Note`.`noteLocalId` = `NoteTag`.`noteLocalId` AND `Note`.`trash` = 0 " +
		"ORDER BY `Note`.`lastUpdate` DESC, `Note`.`noteLocalId` DESC ";

	public final static String NOTELIST_BYKEYWORDS =
		"SELECT `noteLocalId`, `lastUpdate`, `createTime`, `pingYinIndex` " +
		"FROM `Note` WHERE `userLocalId` = ? AND `trash` = 0 " +
		"AND `projectLocalId` = ? " +
		"AND (`title` LIKE ? OR `content` LIKE ? )" +
		"ORDER BY `lastUpdate` DESC,`noteLocalId` DESC ";
	
	public final static String NOTELIST_BYPERSONALSHARE =
		"SELECT `noteLocalId`, `lastUpdate`, `createTime`, `pingYinIndex` " +
		"FROM `Note` " +
		"WHERE `userLocalId` = ? AND `share` = 1 AND `trash` = 0 " +
		"ORDER BY `lastUpdate` DESC,`noteLocalId` DESC ";
	
	public final static String NOTELIST_BYOTHERSSHARE =
		"SELECT `Note`.`noteLocalId`, `NoteShare`.`shareTime`, `NoteShare`.`copyCount`, `Note`.`pingYinIndex`, `NoteShare`.`orderPoint`, " +
		"`NoteShare`.`star` " +
		"FROM `Note`, `NoteShare` " + 
		"WHERE `NoteShare`.`shareUserId` = 0 AND `Note`.`noteId` = `NoteShare`.`noteId` AND `Note`.`trash` = 0 AND `Note`.`share` = 1 " +
		"ORDER BY `NoteShare`.`orderPoint` DESC ";
	
	public final static String NOTELIST_BYUSERSHARE =
		"SELECT `Note`.`noteLocalId`, `NoteShare`.`shareTime`, `NoteShare`.`copyCount`, `Note`.`pingYinIndex`, `NoteShare`.`orderPoint`, " +
		"`NoteShare`.`star` " +
		"FROM `Note`, `NoteShare` " + 
		"WHERE `NoteShare`.`shareUserId` = ? AND `Note`.`noteId` = `NoteShare`.`noteId` AND `Note`.`trash` = 0 AND `Note`.`share` = 1 " +
		"ORDER BY `NoteShare`.`orderPoint` DESC ";

	public final static String NOTELIST_BYONE =
		"SELECT `noteLocalId`, `title`, `lastUpdate`, `syncState`, `contentDigest`, `createTime`, `shortContent`, " +
		"`readPriv`, `writePriv`, `deletePriv`, `managePriv`, `creatorUserId`, `comCount`, `nickName`, `noteId`, " +
		"`thumbnailId`, `sharePassword` " +
		"FROM `Note` WHERE `noteLocalId` = ? ";
	
	public final static String SHARENOTELIST_BYONE =
		"SELECT `noteLocalId`, `title`, `shortContent`, `comCount`, `nickName`, `noteId`, " +
		"`thumbnailId`, `sourceShowText`, `sharePassword` " +
		"FROM `Note` WHERE `noteLocalId` = ? ";
	
	public final static String NOTE_COUNT_KEYWORDS =
		"SELECT count(`noteLocalId`) " +
		"FROM `Note` " +
		"WHERE `trash` = 0 AND `userLocalId` = ? " +
		"AND (`title` LIKE ? OR `content` LIKE ? )";

	public final static String NOTE_ALLINFO =
		"SELECT `title`, `userLocalId`, `catLocalId`, `content`, `contentDigest`, " +
		"`trash`, `client`, `source`, `createTime`, `lastAccess`, " +
		"`lastUpdate`, `accessTimes`, `syncState`, `noteId`, `revision`, " +
		"`share`, `projectLocalId`, " +
		"`readPriv`, `writePriv`, `deletePriv`, `managePriv`, " +
		"`comCount`, `comSyncRevision`, `comLatestRevision`, `allowComment`, " +
		"`shortContent`, `creatorUserId`, " +
		"`lbsLongitude`, `lbsLatitude`, `lbsRadius`, `lbsAddress`, `nickName`, " +
		"`sourceShowText`, `sourceHyperlink`, `sharePassword` " +
		"FROM `Note` WHERE `noteLocalId` = ?";
	
	public final static String NOTE_SIMPLE_INFO =
		"SELECT `title`, `userLocalId`, `catLocalId`, `contentDigest`, " +
		"`trash`, `syncState`, `noteId`, `revision`, `projectLocalId`, " +
		"`creatorUserId`, `nickName` " +
		"FROM `Note` WHERE `noteLocalId` = ?";

	public final static String NOTE_UPDATE_LASTACESS =
		"UPDATE `Note` SET `lastAccess` = ?, `accessTimes` = ? " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_UPDATE_COMSYNCREVISION =
		"UPDATE `Note` SET `comSyncRevision` = ?" +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_UPDATE_COMLATESTSYNCREVISION =
		"UPDATE `Note` SET `comLatestRevision` = ?" +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTES_MOVE_CAT =
		"UPDATE `Note` SET `catLocalId` = ?, `syncState` = 1 " +
		"WHERE `catLocalId` = ?";
	
	public final static String NOTES_REMOVE_FOR_CAT = 
		"UPDATE `Note` SET `syncState` = 1, `trash` = ?, `catLocalId` = ? " +
		"WHERE `catLocalId` = ?";

	public final static String NOTE_UPDATE_SYNCSTATE =
		"UPDATE `Note` SET `syncState`=? WHERE `noteLocalId`=?";

	public final static String NOTE_CAT_MOVE =
		"UPDATE `Note` " +
		"SET `catLocalId` = ? " +
		"WHERE `catLocalId` = ?";
	
	public final static String NOTE_MOVE_CAT =
		"UPDATE `Note` " +
		"SET `catLocalId` = ?, `syncState` = 1 " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_MOVE_USER =
		"UPDATE `Note` SET `userLocalId` = ? WHERE `userLocalId` = ? ";

	public final static String NOTE_BY_CAT =
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `catLocalId` = ? AND `trash` = 0";
	
	public final static String NOTE_BY_SYNCSTATE = 
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `syncState` != 0 AND `noteId` != -1 AND `catLocalId` = ? " +
		"AND `userLocalId` = ? AND `projectLocalId` = ?";
	
	public final static String NOTE_FOR_ADD =
		"SELECT `noteLocalId`, `noteId` FROM `Note` " + 
		"WHERE `syncState` = 1 AND `noteId` = -1 AND `trash` != 1 " +
		"AND `userLocalId` = ? AND `projectLocalId` = ?" + 
		"UNION " +
		"SELECT `Note`.`noteLocalId`, `Note`.`noteId` FROM `Note`,`Attachment` " +
		"WHERE `Attachment`.`syncState` = 1 AND `Attachment`.`attId` = -1 " +
		"AND `Attachment`.`trash` = 0 " +
		"AND `Attachment`.`noteLocalId` = `Note`.`noteLocalId` " +
		"AND `Note`.`userLocalId` = ? AND `projectLocalId` = ?";

	public final static String NOTE_FOR_ADD_ONE =
		"SELECT `noteLocalId`, `noteId` FROM `Note` " + 
		"WHERE `syncState` = 1 AND `noteId` = -1 AND `trash` != 1 AND `noteLocalId` = ? " + 
		"UNION " +
		"SELECT `Note`.`noteLocalId`, `Note`.`noteId` FROM `Note`,`Attachment` " +
		"WHERE `Attachment`.`syncState` = 1 AND `Attachment`.`attId` = -1 AND `Attachment`.`trash` = 0 " +
		"AND `Attachment`.`noteLocalId` = `Note`.`noteLocalId` AND `Note`.`noteLocalId` = ?";

	public final static String NOTE_FOR_ADD_CAT =
		"SELECT `noteLocalId`, `noteId` FROM `Note` " + 
		"WHERE `syncState` = 1 AND `noteId` = -1 AND `trash` != 1 AND `catLocalId` = ? " + 
		"UNION " +
		"SELECT `Note`.`noteLocalId`, `Note`.`noteId` FROM `Note`,`Attachment` " +
		"WHERE `Attachment`.`syncState` = 1 AND `Attachment`.`attId` = -1 AND `Attachment`.`trash` = 0 " +
		"AND `Attachment`.`noteLocalId` = `Note`.`noteLocalId` AND `Note`.`catLocalId` = ?";

	public final static String NOTE_FOR_ADD_CATGROUP =
		"SELECT `noteLocalId`, `noteId` FROM `Note`, `Category` " + 
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " + 
		"`Note`.`syncState` = 1 AND `Note`.`noteId` = -1 AND " +
		"`Note`.`trash` != 1 AND `Category`.`pCatLocalId` = ? " + 
		"UNION " +
		"SELECT `Note`.`noteLocalId`, `Note`.`noteId` " +
		"FROM `Note`, `Attachment`, `Category` " +
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Attachment`.`syncState` = 1 AND `Attachment`.`attId` = -1 AND " + 
		"`Attachment`.`noteLocalId` = `Note`.`noteLocalId` AND " + 
		"`Attachment`.`trash` = 0 AND `Category`.`pCatLocalId` = ?";

	public final static String NOTE_SET_ID =
		"UPDATE `Note` SET `noteId` = ? WHERE `noteLocalId` = ?";

	public final static String NOTE_CONFLICT =
		"UPDATE `Note` SET `noteId` = -1, `syncState` = 1, `revision` = -1 WHERE `noteLocalId` = ?";

	public final static String NOTE_FOR_UPDATE =
		"SELECT `noteLocalId`, `contentDigest`, `noteId`, `revision`, `title`, " +
		"`content` " +
		"FROM `Note` WHERE `userLocalId` = ? AND `projectLocalId` = ? " +
		"AND `syncState` = 1 AND `noteId` != -1";

	public final static String NOTE_FOR_UPDATE_ONE =
		"SELECT `noteLocalId`, `contentDigest`, `noteId`, `revision`, `title`, `content` " +
		"FROM `Note` WHERE `noteLocalId` = ? AND `syncState` = 1 AND `noteId` != -1";

	public final static String NOTE_FOR_UPDATE_CAT =
		"SELECT `noteLocalId`, `contentDigest`, `noteId`, `revision`, `title`, `content` " +
		"FROM `Note` WHERE `catLocalId` = ? AND `syncState` = 1 AND `noteId` != -1";

	public final static String NOTE_FOR_UPDATE_CATGROUP =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, " +
		"`Note`.`revision`, `Note`.`title`, `Note`.`content` " +
		"FROM `Note`, `Category` " +
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Note`.`syncState` = 1 AND `Note`.`noteId` != -1 AND " +
		"`Category`.`pCatLocalId` = ?";

	public final static String NOTE_FOR_SYNCCONTENT =
		"SELECT `noteLocalId`, `contentDigest`, `noteId`, `revision`, `title`, `content` " +
		"FROM `Note` WHERE `userLocalId` = ? AND `projectLocalId` = ? AND `syncState` = 0 " +
		"AND `contentDigest` = '' AND (`trash` = 0 OR `trash` = 2)";

	public final static String NOTE_FOR_SYNCCONTENT_ONE =
		"SELECT `noteLocalId`, `contentDigest`, `noteId`, `revision`, `title`, `content` " +
		"FROM `Note` WHERE `noteLocalId` = ? AND `syncState` = 0 " +
		"AND `contentDigest` = '' AND (`trash` = 0 OR `trash` = 2)";

	public final static String NOTE_FOR_SYNCCONTENT_CAT =
		"SELECT `noteLocalId`, `contentDigest`, `noteId`, `revision`, `title`, `content` " +
		"FROM `Note` WHERE `catLocalId` = ? AND `syncState` = 0 " +
		"AND `contentDigest` = '' AND (`trash` = 0 OR `trash` = 2)";

	public final static String NOTE_FOR_SYNCCONTENT_CATGROUP =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, " +
		"`Note`.`revision`, `Note`.`title`, `Note`.`content` " +
		"FROM `Note`, `Category` " +
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Category`.`pCatLocalId` = ? AND `Note`.`syncState` = 0 AND " +
		"`Note`.`contentDigest` = '' AND (`Note`.`trash` = 0 OR `Note`.`trash` = 2)";

	public final static String NOTE_SET_REVISION =
		"UPDATE `Note` SET `revision` = ? WHERE `noteLocalId` = ?";

	public final static String NOTE_NOTETAG_FORUPDATE =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `NoteTag` " +
		"WHERE `Note`.`userLocalId` = ? AND `Note`.`projectLocalId` = ? " +
		"AND `Note`.`syncState` = 0 AND `Note`.`noteId` != -1 " +
		"AND `NoteTag`.`noteLocalId` = `Note`.`noteLocalId` AND `NoteTag`.`syncState` = 1 " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTETAG_FORUPDATE_ONE =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `NoteTag` " +
		"WHERE `Note`.`noteLocalId` = ? AND `Note`.`syncState` = 0 AND `Note`.`noteId` != -1 " +
		"AND `NoteTag`.`noteLocalId` = `Note`.`noteLocalId` AND `NoteTag`.`syncState` = 1 " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTETAG_FORUPDATE_CAT =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `NoteTag` " +
		"WHERE `Note`.`catLocalId` = ? AND `Note`.`syncState` = 0 AND `Note`.`noteId` != -1 " +
		"AND `NoteTag`.`noteLocalId` = `Note`.`noteLocalId` AND `NoteTag`.`syncState` = 1 " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTETAG_FORUPDATE_CATGROUP =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, " +
		"`Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `NoteTag`, `Category` " +
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Category`.`pCatLocalId` = ? AND `Note`.`syncState` = 0 AND " +
		"`Note`.`noteId` != -1 AND `NoteTag`.`syncState` = 1 AND " +
		"`NoteTag`.`noteLocalId` = `Note`.`noteLocalId` " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTEATT_FORUPDATE =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `Attachment` " +
		"WHERE `Note`.`userLocalId` = ? AND `Note`.`projectLocalId` = ? " +
		"AND `Note`.`syncState` = 0 AND `Note`.`noteId` != -1 " +
		"AND `Attachment`.`noteLocalId` = `Note`.`noteLocalId` AND `Attachment`.`syncState` = 1 " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTEATT_FORUPDATE_ONE =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `Attachment` " +
		"WHERE `Note`.`noteLocalId` = ? AND `Note`.`syncState` = 0 AND `Note`.`noteId` != -1 " +
		"AND `Attachment`.`noteLocalId` = `Note`.`noteLocalId` AND `Attachment`.`syncState` = 1 " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTEATT_FORUPDATE_CAT =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `Attachment` " +
		"WHERE `Note`.`catLocalId` = ? AND `Note`.`syncState` = 0 AND `Note`.`noteId` != -1 " +
		"AND `Attachment`.`noteLocalId` = `Note`.`noteLocalId` AND `Attachment`.`syncState` = 1 " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_NOTEATT_FORUPDATE_CATGROUP =
		"SELECT `Note`.`noteLocalId`, `Note`.`contentDigest`, `Note`.`noteId`, `Note`.`revision`, `Note`.`title` " +
		"FROM `Note`, `Attachment`, `Category` " +
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Category`.`pCatLocalId` = ? AND `Note`.`syncState` = 0 AND " +
		"`Note`.`noteId` != -1 AND `Attachment`.`syncState` = 1 AND " +
		"`Attachment`.`noteLocalId` = `Note`.`noteLocalId` " +
		"GROUP BY `Note`.`noteLocalId`";

	public final static String NOTE_CHECK_ID =
		"SELECT `noteLocalId`, `title` FROM `Note` WHERE `userLocalId` = ? AND `noteId` = ?";
	
	public final static String NOTE_GET_COMSYNCREVISION = 
		"SELECT `comSyncRevision` FROM `Note` WHERE `noteLocalId` = ?";
	
	public final static String NOTE_MINREVISION =
		"SELECT MIN(`revision`) FROM( " +
			"SELECT MIN(`revision`) AS `revision` FROM `Note` " +
			"WHERE `noteLocalId` = ? AND `syncState` = 0 " +
			"UNION " +
			"SELECT MIN(`revision`) AS `revision` FROM `NoteTag` " +
			"WHERE `noteLocalId` = ? AND `syncState` = 0 " +
			"UNION " +
			"SELECT MIN(`revision`) AS `revision` FROM `Attachment` " +
			"WHERE `noteLocalId` = ? AND `syncState` = 0 " +
			")T";
	
	public final static String NOTE_ALLMINREVISION =
		"SELECT MIN(`revision`) FROM( " +
			"SELECT MIN(`revision`) AS `revision` FROM `Note` " +
			"WHERE `userLocalId` = ? AND `syncState` = 0 " +
			"UNION " +
			"SELECT MIN(`NoteTag`.`revision`) AS `revision` FROM `Note`, `NoteTag` " +
			"WHERE `Note`.`userLocalId` = ? " +
			"AND `Note`.`noteLocalId` = `NoteTag`.`noteLocalId` AND `NoteTag`.`syncState` = 0 " +
			"UNION " +
			"SELECT MIN(`Attachment`.`revision`) AS `revision` FROM `Note`, `Attachment` " +
			"WHERE `Note`.`userLocalId` = ? " +
			"AND `Note`.`noteLocalId` = `Attachment`.`noteLocalId` and `Attachment`.`syncState` = 0 " +
			")T";

	public final static String NOTE_SET_OTHERSREVISION =
		"UPDATE `Note` SET `revision` = ? " +
		"WHERE `userLocalId` = ? AND `syncState` = 0";
	
	public final static String NOTE_GET_COM_SYNCREVISION =
		"SELECT `comSyncRevision` FROM `Note` " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_SET_COM_SYNCREVISION =
		"UPDATE `Note` SET `comSyncRevision` = ?" +
		"WHERE `noteLocalId` = ?";
	
//	public final static String NOTE_SET_PRIV_WRITE = 
//		"UPDATE `Note` SET `writePriv` = ?" +
//		"WHERE `noteLocalId` = ?";	
//	
//	public final static String NOTE_SET_PRIV_DELETE =
//		"UPDATE `Note` SET `deletePriv` = ?, `trash` = ?" +
//		"WHERE `noteLocalId` = ?";	
	
	public final static String NOTE_SELCT_FOR_WHITHOUT_NOTESHARE = 
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `share` = 1 AND `userLocalId` = -1 AND `noteId` NOT IN (SELECT `noteId` FROM `NoteShare`)";

	public final static String NOTE_SET_TRASH =
		"UPDATE `Note` SET `trash` = ? " +
		"WHERE `noteLocalId` = ?";
	
	public final static String NOTE_CLEAR =
		"DELETE FROM `Note` WHERE `userLocalId` = ?";
	
	public final static String NOTE_DELETE_PROJECT =
		"DELETE FROM `Note` WHERE `userLocalId` = ? AND `projectLocalId` = ?";

	public final static String NOTE_DELETE_CAT =
		"DELETE FROM `Note` WHERE `catLocalId` = ?";
	
	public final static String NOTE_DELETE_BY_NOTELOCALID=
		"DELETE FROM `Note` WHERE `noteLocalId` = ? ";
	
	public final static String NOTE_DELETE_BY_NOTEID_FROM_NOTESHARE = 
		"DELETE FROM `Note` WHERE `noteId` = ? AND `userLocalId` = -1 ";
	
	public final static String NOTE_DROP_TABLE =
		"DROP TABLE `Note`";

//------------------------------------------------------------------------------
// `NoteTag` Table
	public final static String NOTETAG_CREATE_TABLE =
		"CREATE TABLE `NoteTag`(" +
		"`noteTagId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`noteLocalId` LONG NOT NULL" +
		",`tagLocalId` LONG NOT NULL" +
		",`trash` INTEGER NOT NULL" +
		",`syncState` INTEGER NOT NULL" +
		",`revision` LONG NOT NULL" +
		")";

	public final static String NOTETAG_INSERT =
		"INSERT INTO `NoteTag` " +
		"(`noteLocalId`,`tagLocalId`,`trash`, `syncState`, `revision`) " +
		"VALUES (?, ?, ?, ?, ?)";

	public final static String NOTETAGS_REMOVE =
		"UPDATE `NoteTag` " +
		"SET `trash` = 1, `syncState` = ? " +
		"WHERE `tagLocalId` = ? AND `trash` = 0";

	public final static String NOTETAG_DELETE =
		"UPDATE `NoteTag` " +
		"SET `trash` = 1 " +
		"WHERE `noteLocalId` = ? AND `tagLocalId` = ? AND `trash` = 0";
	
	public final static String NOTETAG_BYNOTE =
		"SELECT `Tag`.`tagLocalId`, `Tag`.`tagName` FROM `NoteTag`, `Tag` " +
		"WHERE `NoteTag`.`tagLocalId` = `Tag`.`tagLocalId` AND " +
		"`noteLocalId`= ? AND `NoteTag`.`trash` = 0";
	
	public final static String NOTETAG_SYNCSTATE =
		"SELECT SUM(`syncState`) FROM `NoteTag` WHERE `noteLocalId` = ?";
	
	public final static String NOTETAG_CHECKNOTETAG = 
		"SELECT `tagLocalId` FROM `NoteTag` " +
		"WHERE `noteLocalId` = ? AND `tagLocalId` = ? AND `trash` = 0";

	public final static String NOTETAG_UPDATE_SYNCSTATE =
		"UPDATE `NoteTag` SET `syncState`=? WHERE `noteLocalId` = ? AND `tagLocalId` = ? ";

	public final static String NOTETAG_CONFLICT =
		"UPDATE `NoteTag` SET `syncState`=1, `revision`=-1 WHERE `noteLocalId` = ? ";

	public final static String NOTETAG_CHANGETAG =
		"UPDATE `NoteTag` SET `tagLocalId` = ? " +
		"WHERE `tagLocalId` = ?";
	
	public final static String NOTETAG_USING =
		"SELECT `noteTagId` FROM `NoteTag` " +
		"WHERE `tagLocalId` = ? AND `trash` = 0";
	
	public final static String NOTETAG_FORUPDATE =
		"SELECT `NoteTag`.`tagLocalId`, `Tag`.`tagId`, `NoteTag`.`trash`, `NoteTag`.`revision` " +
		"FROM `NoteTag`, `Tag` " +
		"WHERE `NoteTag`.`noteLocalId` = ? AND `NoteTag`.`syncState` = 1 " +
		"AND `NoteTag`.`tagLocalId` = `Tag`.`tagLocalId`";

	public final static String NOTETAG_SET_REVISION =
		"UPDATE `NoteTag` SET `revision` = ? " +
		"WHERE `noteLocalId` = ? AND `syncState` = ?";

	public final static String NOTETAG_SET_OTHERSREVISION =
		"UPDATE `NoteTag` SET `revision` = ? " +
		"WHERE `syncState` = 0 AND " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userLocalId` = ?)";

	public final static String NOTETAG_SET_UPDATEREVISION =
		"UPDATE `NoteTag` " +
		"SET `syncState` = 0, `revision` = ? " +
		"WHERE `noteLocalId` = ? AND `syncState` = 1";

	public final static String NOTETAG_SELECT =
		"SELECT `noteTagId`, `trash` FROM `NoteTag` " +
		"WHERE `noteLocalId` = ? AND `tagLocalId` = ?";

	public final static String NOTETAG_UPDATE =
		"UPDATE `NoteTag` SET `trash` = ?, `syncState` = ?, `revision` = ? " +
		"WHERE `noteTagId` = ? ";

	public final static String NOTETAG_CLEAR =
		"DELETE FROM `NoteTag` WHERE " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userLocalId`=?)";

	public final static String NOTETAG_SET_TRASH_BYNOTE =
		"UPDATE `NoteTag` SET `trash` = ? " +
		"WHERE `noteLocalId` = ? ";
	
	public final static String NOTETAG_REAL_DELETE = 
		"DELETE FROM `NoteTag` " +
		"WHERE `tagLocalId` = ?";
	
	public final static String NOTETAG_DROP_TABLE = 
		"DROP TABLE `NoteTag`";

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
		",`downloadSize` INTEGER NOT NULL" +
		",`trash` INTEGER NOT NULL" +
		",`syncState` INTEGER NOT NULL" +
		",`createTime` LONG NOT NULL" +
		",`lastAccess` LONG NOT NULL" +
		",`accessTimes` INTEGER NOT NULL" +
		",`digest` TEXT(32) NOT NULL" +
		",`uploadFlag` INTEGER NOT NULL" +
		",`serverUploadFlag` INTEGER NOT NULL" +
		",`attId` LONG NOT NULL" +
		",`revision` LONG NOT NULL" +
		",`width` INTEGER NOT NULL" +
		",`height` INTEGER NOT NULL" +
		",`netdiskFlag` INTEGER NOT NULL DEFAULT -1" +
		",`netdiskUrl` TEXT(200) NOT NULL DEFAULT ''" + 
		")";
	
	public final static String ATT_INSERT = 
		"INSERT INTO `Attachment` " +
		"(`attName`, `type`, `path`, `noteLocalId`, `size`, `downloadSize`, " +
		"`trash`, `syncState`, `createTime`, `lastAccess`, `accessTimes`, " +
		"`digest`, `uploadFlag`, `serverUploadFlag`, `attId`, `revision`, " +
		"`width`, `height`, `netdiskFlag`, `netdiskUrl`) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + 
		"?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 

	public final static String ATT_COUNT =
		"SELECT COUNT(`attLocalId`) FROM `Attachment` " +
		"WHERE `noteLocalId`= ? AND `trash` = 0";

	public final static String ATT_SELECT =
		"SELECT `attLocalId`,`attName`, `type`, `path`, `size`, " +
		"`createTime`, `lastAccess`, `accessTimes`, `digest`, `uploadFlag`, " +
		"`serverUploadFlag`, `attId`, `downloadSize`, `width`, `height`, `netdiskFlag`, `netdiskUrl` " +
		"FROM `Attachment` WHERE `noteLocalId`= ? AND `trash` = 0";
	
	public final static String ATT_SELECT_BYID =
		"SELECT `attLocalId`,`attName`, `type`, `path`, `size`, " +
		"`createTime`, `lastAccess`, `accessTimes`, `digest`, `uploadFlag`, " +
		"`serverUploadFlag`, `attId`, `downloadSize`, `width`, `height`, `netdiskFlag` " +
		"FROM `Attachment` WHERE `attLocalId`= ?";
	
	public final static String ATT_SELECT_BY_NOTE = 
		"SELECT `attLocalId`, `type` " +
		"FROM `Attachment` WHERE `noteLocalId`= ?";
	
	public final static String ATT_SELECT_BY_WITHOUT_NOTESHARE =
		"SELECT `attLocalId`, `type`, `noteLocalId` " +
		"FROM `Attachment` WHERE " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` " +
		"WHERE `share` = 1 AND `userLocalId` = -1 AND `noteId` NOT IN (SELECT `noteId` FROM `NoteShare`))";
	
	public final static String ATT_SELECT_WH =
		"SELECT `attLocalId`, `path` FROM `Attachment` " +
		"WHERE `width` > ? AND `height` > ? AND `noteLocalId`= ? AND `trash` = 0 ";
	
	public final static String ATT_SYNCSTATE =
		"SELECT SUM(`syncState`) FROM `Attachment` WHERE `noteLocalId` = ?";
	
	public final static String  ATT_UPLOADFLAG =
		"SELECT `uploadFlag` FROM `Attachment` WHERE `noteLocalId` = ? AND `trash` = 0";

	public final static String ATT_UPDATE_SYNCSTATE =
		"UPDATE `Attachment` SET `syncState`=? WHERE `attLocalId`=?";

	public final static String ATT_SAVED =
		"UPDATE `Attachment` " +
		"SET `path` = ?, `uploadFlag` = -1, `downloadSize` = ?" +
		"WHERE `attLocalId` = ?";

	public final static String ATT_NEW_COUNT =
		"SELECT COUNT(`attLocalId`) FROM `Attachment` " +
		"WHERE `noteLocalId` = ? AND `syncState` = 1 AND `trash` = 0";

	public final static String ATT_FOR_ADD =
		"SELECT `attLocalId` FROM `Attachment` " +
		"WHERE `noteLocalId` = ? AND `syncState` = 1 AND `trash` = 0";

	public final static String ATT_SET_ID =
		"UPDATE `Attachment` SET `attId` = ? WHERE `attLocalId` = ?";

	public final static String ATT_CONFLICT =
		"UPDATE `Attachment` SET `attId`=-1, `syncState`=1, `revision`=-1, " +
		"`uploadFlag` = -1, `serverUploadFlag` = -1, `netdiskFlag` = 0 " +
		"WHERE `noteLocalId` = ? ";

	public final static String ATT_FORUPDATE =
		"SELECT `attLocalId`, `attName`, `type`, `size`, `trash`, " +
		"`createTime`, `lastAccess`, `accessTimes`, `digest`, `attId`, `revision` " +
		"FROM `Attachment` " +
		"WHERE `noteLocalId` = ? AND `syncState` = 1 AND `attId` != -1";

	public final static String ATT_SET_REVISION =
		"UPDATE `Attachment` SET `revision` = ? " +
		"WHERE `noteLocalId` = ? AND `syncState` = ? AND `attId` != -1";

	public final static String ATT_SET_OTHERSREVISION =
		"UPDATE `Attachment` SET `revision` = ? " +
		"WHERE `syncState` = 0 AND " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userLocalId` = ?)";

	public final static String ATT_SET_UPDATEREVISION =
		"UPDATE `Attachment` " +
		"SET `syncState` = 0, `revision` = ? " +
		"WHERE `noteLocalId` = ? AND `syncState` = 1";

	public final static String ATT_UPDATE =
		"UPDATE `Attachment` " +
		"SET `attName` = ?, `type` = ?, `size` = ?, `trash` = ?, `createTime` = ?, " +
		"`lastAccess` = ?, `accessTimes` = ?, `digest` = ?, `serverUploadFlag` = ?, " +
		"`attId` = ?, `revision` = ?, `netdiskFlag` = ?, `netdiskUrl` = ? " +
		" WHERE `attLocalId` = ?";

	public final static String ATT_FORDOWNLOAD = 
		"SELECT `attLocalId`, `type`, `size`, `downloadSize`, `digest`, `attId`, `netdiskFlag`, `serverUploadFlag`, `netdiskUrl` " +
		"FROM `Attachment` WHERE `syncState` = 0 AND `uploadFlag` > 0 AND `trash` = 0 " +
		"AND (`serverUploadFlag` = 0 OR `netdiskFlag` > 0) " +
		"AND `noteLocalId` IN (" +
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `userLocalId` = ? AND `projectLocalId` = ?) " +
		"ORDER BY `size` ASC";
	
	public final static String ATT_FORDOWNLOAD_ONE = 
		"SELECT `attLocalId`, `type`, `size`, `downloadSize`, `digest`, `attId`, `netdiskFlag`, `serverUploadFlag`, `netdiskUrl` " +
		"FROM `Attachment` WHERE `syncState` = 0 AND `uploadFlag` > 0 AND `trash` = 0 " +
		"AND (`serverUploadFlag` = 0 OR `netdiskFlag` > 0) " +
		"AND `noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `noteLocalId` = ?) " +
		"ORDER BY `size` ASC";

	public final static String ATT_FORDOWNLOAD_CAT = 
		"SELECT `attLocalId`, `type`, `size`, `downloadSize`, `digest`, `attId`, `netdiskFlag`, `serverUploadFlag`, `netdiskUrl` " +
		"FROM `Attachment` WHERE `syncState` = 0 AND `uploadFlag` > 0 AND `trash` = 0 " +
		"AND (`serverUploadFlag` = 0 OR `netdiskFlag` > 0) " +
		"AND `noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `catLocalId` = ?) " +
		"ORDER BY `size` ASC";

	public final static String ATT_FORUPLOAD =
		"SELECT `attLocalId`, `path`, `digest`, `attId`, `size` " +
		"FROM `Attachment` " +
		"WHERE `syncState` = 0 AND `uploadFlag` = -1 AND `trash` = 0 " +
		"AND `noteLocalId` IN (" +
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `userLocalId` = ? AND `projectLocalId` = ?)";

	public final static String ATT_FORUPLOAD_ONE =
		"SELECT `attLocalId`, `path`, `digest`, `attId`, `size` " +
		"FROM `Attachment` WHERE `syncState` = 0 AND `uploadFlag` = -1 AND `trash` = 0 " +
		"AND `noteLocalId` = ?";

	public final static String ATT_FORUPLOAD_CAT =
		"SELECT `attLocalId`, `path`, `digest`, `attId`, `size` " +
		"FROM `Attachment` WHERE `syncState` = 0 AND `uploadFlag` = -1 AND `trash` = 0 " +
		"AND `noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `catLocalId` = ?)";

	public final static String ATT_FORUPLOAD_CATGROUP =
		"SELECT `attLocalId`, `path`, `digest`, `attId`, `size` " +
		"FROM `Attachment` WHERE `syncState` = 0 AND `uploadFlag` = -1 AND `trash` = 0 " +
		"AND `noteLocalId` IN ( " +
		"SELECT `Note`.`noteLocalId` FROM `Note`, `Category` " +
		"WHERE `Note`.`catLocalId` = `Category`.`catLocalId` AND " +
		"`Category`.`pCatLocalId` = ? )";

	public final static String ATT_SET_UPLOADFLAG =
	 	"UPDATE `Attachment` SET `uploadFlag` = ?, `serverUploadFlag` = ?, `revision` = ? " +
	 	"WHERE `attLocalId` = ?";
	
	public final static String ATT_SET_NETDISKFLAG = 
		"UPDATE `Attachment` SET `netdiskFlag` = ? " +
		"WHERE `attLocalId` = ?";

	public final static String ATT_CHECK_ID = 
		"SELECT `attLocalId`, `syncState` FROM `Attachment` WHERE `attId` = ?";
	
	public final static String ATT_CHECK_ID_FORUSER =
		"SELECT `Note`.`userLocalId`, `Attachment`.`attLocalId` FROM `Note`, `Attachment` " +
		"WHERE `Note`.`noteLocalId` = `Attachment`.`noteLocalId` " +
		"AND `attId` = ?"
		;
	
	public final static String ATT_GET_NOTEID = 
		"SELECT `Note`.`noteId`, `Note`.`noteLocalId` FROM `Note`, `Attachment` " +
		"WHERE `Note`.`noteLocalId` = `Attachment`.`noteLocalId` " +
		"AND `attLocalId` = ?";	
	
	public final static String ATT_SET_STATUS = 
		"UPDATE `Attachment` SET `path` = ?, `uploadFlag` = ?, `downloadSize` = ?, " +
		"`syncState` = ?, `serverUploadFlag` = ?, `revision` = ? " +
		"WHERE `attLocalId` = ?";

	public final static String ATT_SET_DOWNLOADED =
		"UPDATE `Attachment` SET `path` = ?, `uploadFlag` = ?, `downloadSize` = ?, " +
		"`width` = ?, `height` = ? " +
		"WHERE `attLocalId` = ?";

	public final static String ATT_FORCLEAR =
		"SELECT `attLocalId`, `type` " +
		"FROM `Attachment` WHERE " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userLocalId`=?)";

	public final static String ATT_IN_NOTE =
		"SELECT `attLocalId`, `type` " +
		"FROM `Attachment` WHERE `noteLocalId` = ?";

	public final static String ATT_CLEAR =
		"DELETE FROM `Attachment` WHERE " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userLocalId`=?)";

	public final static String ATT_SET_TRASH =
		"UPDATE `Attachment` SET `trash` = ? WHERE `attLocalId` = ?";

	public final static String ATT_SET_TRASH_BYNOTE =
		"UPDATE `Attachment` SET `trash` = ? WHERE `noteLocalId` = ?";
	
	public final static String ATT_SET_TRASH_SYNCSTATE = 
		"UPDATE `Attachment` SET `trash` = ?, `syncState` = ? WHERE `attLocalId` = ?";
	
	public final static String ATT_DELETE_PROJECT =
		"DELETE FROM `Attachment` " +
		"WHERE `noteLocalId` IN (" +
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `userLocalId`= ? AND `projectLocalId` = ?)";

	public final static String ATT_DELETE_CAT =
		"DELETE FROM `Attachment` " +
		"WHERE `noteLocalId` IN (" +
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `catLocalId`= ? )";
	public final static String ATT_DELETE_NOTE =
		"DELETE FROM `Attachment` " +
		"WHERE `noteLocalId` = ?";
	
	public final static String ATT_DROP_TABLE = 
		"DROP TABLE `Attachment`";

//------------------------------------------------------------------------------
// `Binding` Table
	public final static String BINDING_CREATE_TABLE =
		"CREATE TABLE `Binding`(" +
		"`bindingLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`bindingId` LONG NOT NULL" +
		",`userLocalId` INTEGER NOT NULL" +
		",`bType` INTEGER NOT NULL" +
		",`bId` TEXT(50) NOT NULL" +
		",`bVerify` TEXT(32) NOT NULL" +
		",`accessToken` TEXT(200) NOT NULL" +
		",`showName` TEXT(50) NOT NULL" +
		",`authType` INTEGER NOT NULL" +
 		",`revision` LONG NOT NULL" +
		")";

	public final static String BINDING_INSERT =
		"INSERT INTO `Binding` " +
		"(`bindingId`, `userLocalId`, `bType`, `bId`, `bVerify`, `accessToken`, `showName`, " +
		"`authType`, `revision`) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
	
	public final static String BINDING_UPDATE = 
		"UPDATE `Binding` SET `bindingId` = ?, `accessToken` = ?, " +
		"`showName` = ?, `authType` = ?, `revision` = ? " +
		"WHERE `bindingLocalId` = ?";
	
	public final static String BINDING_SELECT_BYTYPE =
		"SELECT `bindingLocalId`, `bindingId`, `userLocalId`, `bType`, `bId`, `bVerify`, `accessToken`, " +
		"`showName`, `authType`, `revision` " +
		"FROM `Binding` WHERE `userLocalId` = ? AND `bType`=?" ;
	
	public final static String BINDING_CHECK_BID =
		"SELECT `bindingLocalId` FROM `Binding` " + 
		"WHERE `userLocalId` = ? AND `bType`=? AND `bId`=? ";
	
	public final static String BINDING_DROP_TABLE = 
		"DROP TABLE `Binding`";

//------------------------------------------------------------------------------
//`Project` Table
	public final static String PROJECT_CREATE_TABLE=
		"CREATE TABLE `Project`(" +
		"`projectLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`projectId` LONG NOT NULL" +
		",`projectName` TEXT(32) NOT NULL" +
		",`intro` TEXT(100) NOT NULL" +
		",`userLocalId` LONG NOT NULL"+
		",`readPriv` INTEGER NOT NULL" +
		",`writePriv` INTEGER NOT NULL" +
		",`catSyncRevision` LONG NOT NULL" +
		",`noteSyncRevision` LONG NOT NULL" +
		",`createPriv` INTEGER NOT NULL " +
		",`deletePriv` INTEGER NOT NULL" +
		",`managePriv` INTEGER NOT NULL" +
		",`unreadCount` INTEGER NOT NULL DEFAULT 0" +
		",`nikeName` TEXT(50) NOT NULL DEFAULT ''" +
		")";
	
	public final static String PROJECT_CREATE_ORIGINAL_TABLE=
			"CREATE TABLE `Project`(" +
			"`projectLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
			",`projectId` LONG NOT NULL" +
			",`projectName` TEXT(32) NOT NULL" +
			",`intro` TEXT(100) NOT NULL" +
			",`userLocalId` LONG NOT NULL"+
			",`readPriv` INTEGER NOT NULL" +
			",`writePriv` INTEGER NOT NULL" +
			",`catSyncRevision` LONG NOT NULL" +
			",`noteSyncRevision` LONG NOT NULL" +
			",`createPriv` INTEGER NOT NULL " +
			",`deletePriv` INTEGER NOT NULL" +
			",`managePriv` INTEGER NOT NULL" +
			")";
	
	public final static String PROJECT_INSERT = 
		"INSERT INTO `Project` " +
		"(`projectId`, `projectName`, `intro`, `userLocalId`, `readPriv`, " +
		"`writePriv`, `catSyncRevision`, `noteSyncRevision`, " +
		"`createPriv`, `deletePriv`, `managePriv`, `unreadCount`, `nikeName`) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?)"; 

	public final static String PROJECT_GET = 
		"SELECT `projectLocalId`, `projectId`, `projectName`, `intro`, " +
		"`readPriv`, `writePriv`, `catSyncRevision`, " +
		"`noteSyncRevision`, " +
		"`createPriv`, `deletePriv`, `managePriv`, `unreadCount`, `nikeName` " +
		"FROM `Project` " + 
		"WHERE `userLocalId` = ?";
	
	public final static String PROJECT_COUNT =
		"SELECT count(`projectLocalId`) " +
		"FROM `Project` " +
		"WHERE `userLocalId` = ? ";

	public final static String PROJECT_GET_BYID = 
		"SELECT `projectLocalId`, `projectId`, `projectName`, `intro`, " +
		"`readPriv`, `writePriv`, `catSyncRevision`, " +
		"`noteSyncRevision`, " +
		"`createPriv`, `deletePriv`, `managePriv` " +
		"FROM `Project` " + 
		"WHERE `projectLocalId` = ?";

	public final static String PROJECT_CHECK_ID =
		"SELECT `projectLocalId` FROM `Project` " +
		"WHERE `projectId` = ? AND `userLocalId` = ?";
	
	public final static String PROJECT_SELECT_FOR_NOTEID = 
		"SELECT `Category`.`projectLocalId` " +
		"FROM `Category`, `Note` " +
		"WHERE `Category`.`catLocalId` = `Note`.`catLocalId` AND `Note`.`noteId` = ?";

	public final static String PROJECT_UPDATE = 
		"UPDATE `Project` " +
		"SET `projectName` = ?, `intro` = ?, `readPriv` = ?, `writePriv` = ?, " +
		"`createPriv` = ?, `deletePriv` = ?, `managePriv` = ?, `unreadCount` = ?, `nikeName` = ? " +
		"WHERE `projectLocalId` = ?";
	
	public final static String PROJECT_SET_PRIV_CREATE = 
		"UPDATE `Project` " +
		"SET `createPriv` = ? " +
		"WHERE `projectLocalId` = ?";
	
	public final static String PROJECT_SET_PRIV_WRITE = 
		"UPDATE `Project` " +
		"SET `writePriv` = ? " +
		"WHERE `projectLocalId` = ?";
	
	public final static String PROJECT_SET_PRIV_READ = 
		"UPDATE `Project` " +
		"SET `readPriv` = ? " +
		"WHERE `projectLocalId` = ?";
	
	public final static String PROJECT_SET_UNREADCOUNT = 
		"UPDATE `Project` " +
		"SET `unreadCount` = ? " +
		"WHERE `projectLocalId` = ?";
	
	public final static String PROJECT_DELETE_PROJECT =
		"DELETE FROM `Project` " +
		"WHERE `userLocalId`= ? AND `projectLocalId` = ?";

	public final static String PROJECT_SELECT_SYNC_REVISION =
		"SELECT `noteSyncRevision`, `catSyncRevision` " +
		"FROM `Project` " +
		"WHERE `projectLocalId` = ?";

	public final static String PROJECT_SET_NOTESYNCREVISION =
		"UPDATE `Project` SET `noteSyncRevision` = ? " +
		"WHERE `projectLocalId` = ?";

	public final static String PROJECT_SET_CATSYNCREVISION =
		"UPDATE `Project` SET `catSyncRevision` = ? " +
		"WHERE `projectLocalId` = ?";

	public final static String PROJECT_CLEAR =
		"DELETE FROM `Project` WHERE `userLocalId` = ?";
	
	public final static String PROJECT_DROP_TABLE = 
		"DROP TABLE `Project`";
	
//------------------------------------------------------------------------------------------------
//`Comment` Table
	public final static String COMMENT_CREATE_TABLE = 
		"CREATE TABLE `Comment`(" +
		"`commentLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`commentId` LONG NOT NULL" +
		",`userId` LONG NOT NULL" +
		",`nickName` TEXT(50) NOT NULL" +
		",`noteLocalId` INTEGER NOT NULL" +
		",`content` TEXT(500) NOT NULL" +
		",`postTime` LONG NOT NULL" +
		",`status` INTEGER NOT NULL" +
		",`revision` LONG NOT NULL " +
		")";
	
	public final static String COMMENT_CREATE_TABLE_NEW = 
			"CREATE TABLE `Comment`(" +
			"`commentLocalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
			",`commentId` LONG NOT NULL" +
			",`userId` LONG NOT NULL" +
			",`nickName` TEXT(50) NOT NULL" +
			",`noteLocalId` INTEGER NOT NULL" +
			",`content` TEXT(500) NOT NULL" +
			",`postTime` LONG NOT NULL" +
			",`status` INTEGER NOT NULL" +
			",`revision` LONG NOT NULL " +
			",`email` TEXT(100) NOT NULL DEFAULT ''" +
			")";
	
	public final static String COMMENT_INSERT =
		"INSERT INTO `Comment` " + 
		"(`commentId`, `userId`, `nickName`, `noteLocalId`, " +
		"`content`, `postTime`, `status`, `revision`, `email`)"+
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public final static String COMMENT_GET_BYID = 
		"SELECT `commentId`, `userId`, `nickName`, `noteLocalId`, " +
		"`content`, `postTime`, `status`, `revision` "+
		"FROM Comment " + 
		"WHERE `commentLocalId` = ?";
	
	public final static String COMMENT_GET_BYNOTE =
		"SELECT `commentLocalId`, `commentId`, `userId`, `nickName`, " +
		"`content`, `postTime`, `status`, `revision`, `email` "+
		"FROM Comment " +
		"WHERE `noteLocalId` = ? " +
		"ORDER BY `postTime` DESC,`commentId` DESC ";
	
	public final static String COMMENT_UPDATE =
		"UPDATE `Comment` " +
		"SET `commentId` = ?, `userId` = ?, `nickName` = ?, " +
		"`content` = ?, `postTime` = ?, `status` = ?, `email` = ? " +
		"WHERE `commentLocalId` = ?";
	
	public final static String COMMENT_UPDATE_FOR_SYNCADD = 
		"UPDATE `Comment` " +
		"SET `commentId` = ?, `postTime` = ?, `revision` = ?" +
		"WHERE `commentLocalId` = ?";
	
	public final static String COMMENT_CHECK_ID =
		"SELECT `commentLocalId` " +
		"FROM Comment " +
		"WHERE `commentId` = ? AND `noteLocalId` = ?";
	
	public final static String COMMENT_SET_STATUS =
		"UPDATE `Comment` " +
		"SET `status` = ? " +
		"WHERE `commentLocalId` = ?";
	
	public final static String COMMENT_CLEAR =
		"DELETE FROM `Comment` WHERE " +
		"`noteLocalId` IN (SELECT `noteLocalId` FROM `Note` WHERE `userLocalId`=?)";
	
	public final static String COMMENT_DELETE_PROJECT =
		"DELETE FROM `Comment` " +
		"WHERE `noteLocalId` IN (" +
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `userLocalId`= ? AND `projectLocalId` = ?)";
	
	public final static String COMMENT_DELETE_CAT =
		"DELETE FROM `Comment` " +
		"WHERE `noteLocalId` IN (" +
		"SELECT `noteLocalId` FROM `Note` " +
		"WHERE `catLocalId`= ? )";
	
	public final static String COMMENT_CLEAR_BYNOTE = 
		"DELETE FROM `Comment` WHERE `noteLocalId` = ?";
	
	public final static String COMMENT_DROP_TABLE = 
		"DROP TABLE `Comment`";
	
	
//--------------------------------------------------------------------------------------
//	`UnreadNote` Table
	public final static String UNREADNOTE_CREATE_TABLE =
		"CREATE TABLE `UnreadNote`(" +
		"`unreadId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`projectLocalId` LONG NOT NULL" +
		",`userLocalId` LONG NOT NULL" +
		",`noteId` LONG NOT NULL" +
		",`nickname` TEXT(50) NOT NULL" +
		",`unreadTime` LONG NOT NULL" +
		")";
	
	public final static String UNREADNOTE_CREATE_TABLE_NEW =
			"CREATE TABLE `UnreadNote`(" +
			"`unreadId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
			",`projectLocalId` LONG NOT NULL" +
			",`userLocalId` LONG NOT NULL" +
			",`noteId` LONG NOT NULL" +
			",`nickname` TEXT(50) NOT NULL" +
			",`unreadTime` LONG NOT NULL" +
			",`updateType` TEXT(32) NOT NULL DEFAULT ''" +
			",`userId` LONG NOT NULL DEFAULT 0" + 
			")";
	
	public final static String UNREADNOTE_INSERT = 
		"INSERT INTO `UnreadNote` " + 
		"(`projectLocalId`, `userLocalId`, `noteId`, `nickname`, `unreadTime`, `updateType`, `userId`) "+
		"VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	public final static String UNREADNOTE_GET = 
		"SELECT `unreadId`, `noteId`, `nickname`, `unreadTime`, `updateType`, `userId` " +
		"FROM `UnreadNote` " +
		"WHERE `projectLocalId` = ? AND `userLocalId` = ? ORDER BY `unreadTime` DESC";
	
	public final static String UNREADNOTE_COUNT = 
		"SELECT COUNT(`noteId`)" +
		"FROM `UnreadNote` " +
		"WHERE `userLocalId` = ?";
	
	public final static String UNREADNOTE_CHECK_NOTEID = 
		"SELECT `noteId` " +
		"FROM `UnreadNote` " +
		"WHERE `noteId` = ?";
	
	public final static String UNREADNOTE_DELETE =
		"DELETE FROM `UnreadNote` " +
		"WHERE `noteId` = ? AND `projectLocalId` = ? AND `userLocalId` = ?";
	
	public final static String UNREADNOTE_DELETE_FORID = 
		"DELETE FROM `UnreadNote` WHERE `unreadId` = ?";
	
	public final static String UNREADNOTE_CLEAR_BYPROJECT = 
		"DELETE FROM `UnreadNote` " +
		"WHERE `projectLocalId` = ? AND `userLocalId` = ?";
	
	public final static String UNREADNOTE_CLEAR_WITHOUT_APPROVE =
		"DELETE FROM `UnreadNote` " +
		"WHERE `projectLocalId` = ? AND `userLocalId` = ? AND `updateType` != 'join'";
	
	public final static String UNREADNOTE_CLEAR = 
		"DELETE FROM `UnreadNote` " +
		"WHERE `userLocalId` = ?";
	
	public final static String UNREADNOTE_DROP_TABLE = 
		"DROP TABLE `UnreadNote`";

//--------------------------------------------------------------------------------------
//	`NoteShare` Table
	public final static String NOTESHARE_CREATE_TABLE =
		"CREATE TABLE `NoteShare`(" +
		"`noteShareId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`noteId` LONG NOT NULL" +
		",`star` INTEGER NOT NULL" +
		",`shareTime` LONG NOT NULL" +
		")";
	
	public final static String NOTESHARE_CREATE_NEWTABLE = 
		"CREATE TABLE `NoteShare`(" +
		"`noteShareId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" +
		",`noteId` LONG NOT NULL" +
		",`star` INTEGER NOT NULL" +
		",`shareTime` LONG NOT NULL" +
		",`copyCount` INTEGER NOT NULL DEFAULT 0" +
		",`orderPoint` LONG NOT NULL DEFAULT 0" +
		",`shareUserId` LONG NOT NULL DEFAULT 0" + 
		")";
	
	public final static String NOTESHARE_INSERT = 
		"INSERT INTO `NoteShare`" +
		"(`noteId`, `star`, `shareTime`, `copyCount`, `orderPoint`, `shareUserId`) " +
		"VALUES (?, ?, ?, ?, ?, ?)";
	
	public final static String NOTESHARE_SELECT = 
		"SELECT `noteShareId`, `noteId`, `star`, `shareTime`, `copyCount`, `orderPoint` " +
		"FROM `NoteShare`";
	
	public final static String NOTESHARE_MIN_ORDERPOINT = 
		"SELECT MIN(`orderPoint`) FROM `NoteShare` WHERE `orderPoint` != 0 AND `shareUserId` = ? ";
	
	public final static String NOTESHARE_UPDATE =
		"UPDATE `NoteShare` " +
		"SET `star` = ?, `shareTime` = ?, `copyCount` = ?, `orderPoint` = ?, `shareUserId` = ? " +
		"WHERE `noteId` = ?";
	
	public final static String NOTESHARE_GET_BY_NOTEID =
		"SELECT `noteShareId`, `star`, `shareTime`, `copyCount`, `orderPoint` " +
		"FROM `NoteShare` WHERE `noteId` = ? AND `shareUserId` = ?";
	
	public final static String NOTESHARE_DELETE_WITHOUT_USER = 
		"DELETE FROM `NoteShare` WHERE `shareUserId` != 0 ";
	
	public final static String NOTESHARE_CLEAR_WITHUSER = 
		"DELETE FROM `NoteShare` WHERE `shareUserId` = ? ";
	
	public final static String NOTESHARE_CLEAR = 
		"DELETE FROM `NoteShare`";
	
}
