package edu.uci.classes.util.classes;

/**
 * List of options for how the given Class should sync to the SQLite Database.
 *
 * @see Class
 * @author Qhucy
 * @author Discord: Qhucy#2279
 */
public enum DatabaseSyncOption
{

    /**
     * No sync action is needed.
     */
    NONE,

    /**
     * Insert a new row into the Database for the given Class.
     */
    CREATE,

    /**
     * Update the row in the Database for the given Class.
     */
    UPDATE,

    /**
     * Delete the Class' row from the Database.
     */
    DELETE

}