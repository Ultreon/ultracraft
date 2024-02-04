package com.ultreon.craft.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Paged list, page 0 is the first page.
 * Page size 1 == 1 object, 5 == 5 objects.
 *
 * @author Qboi123
 * @param <T> the type of items in the list.
 */
public class PagedList<T> extends ArrayList<T> {
    private final int pageSize;

    public PagedList(int pageSize, int initialCapacity) {
        super(initialCapacity);
        this.pageSize = pageSize;
    }

    public PagedList(int pageSize) {
        super();
        this.pageSize = pageSize;
    }

    public PagedList(int pageSize, @NotNull Collection<? extends T> collection) {
        super(collection);
        this.pageSize = pageSize;
    }

    public PagedList(@NotNull PagedList<? extends T> paginatedList) {
        super(paginatedList);
        this.pageSize = paginatedList.pageSize;
    }

    public List<T> getFullPage(int page) {
        int startIndex = page * this.pageSize;
        return super.subList(startIndex, Math.min(startIndex + this.pageSize, super.size()));
    }

    /**
     * Get the object at the page and index.
     *
     * @see ArrayList#get(int)
     * @param page page number, 0 is first.
     * @param index index at that page.
     * @return the object at the page and index.
     */
    public T get(int page, int index) {
        if (index >= this.pageSize) {
            return null;
        }

        return super.get((page * this.pageSize) + index);
    }
}
