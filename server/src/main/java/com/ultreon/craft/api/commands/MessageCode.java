package com.ultreon.craft.api.commands;

public enum MessageCode {
    EMPTY("UC-0", 0),
    NO_PERMISSION("UC-2", 2),
    INVALID_VALUE("UC-4", 4),
    NEED_PLAYER("UC-5", 5),
    NEED_ENTITY("UC-6", 6),
    NEED_CONSOLE("UC-7", 7),
    NO_SELECTION("UC-8", 8),
    NO_TARGET("UC-9", 9),
    NOT_OP("UC-10", 10),
    OUT_OF_RANGE("UC-11", 11),
    OVERLOAD("UC-12", 12),
    CONFLICT("UC-13", 13),
    IMPOSSIBLE("UC-15", 15),
    CANNOT_BE_SELF("UC-17", 17),
    NOT_FOUND_IN_WORLD("UC-18", 18),
    TOO_FEW_ARGS("UC-19", 19),
    TOO_MANY_ARGS("UC-20", 20),
    SELECTOR_TOO_SMALL("UC-21", 21),
    COMMAND_OUTPUT_DETECTED("UC-22", 22),
    ACCESS_DENIED("UC-403", 403),
    NOT_FOUND("UC-404", 404),
    REJECTED("UC-410", 410),
    OVERFLOW("UC-300", 300),
    SERVER_ERROR("UC-500", 500),
    HACKER("UC-503", 503),
    COOLDOWN("UC-599", 599),
    DOES_NOT_MAKE_SENSE("UC-508", 508),
    GENERIC("UC-999", 999),
    WIP("UC-1000", 1000),
    OUTDATED("UC-1001", 1001),
    DEPRECATED("UC-1002", 1002),
    EXPERIMENTAL("UC-1003", 1003),
    DANGEROUS("UC-1004", 1004),
    EDIT_MODE("UC-1005", 1005);

    private final String id;
    private final int code;

    MessageCode(String id, int code) {
        this.id = id;
        this.code = code;
    }

    public String getId() {
        return this.id;
    }

    public int getCode() {
        return this.code;
    }
}