package com.example.EduPOP.domain.common;

import lombok.Getter;

@Getter
public class Paging {

    private final int currentPage;
    private final int pageSize;

    private final int totalCount;
    private final int totalPage;

    private final int startPage;
    private final int endPage;

    private final int offset;

    public Paging(int currentPage, int pageSize, int totalCount) {

        this.pageSize = pageSize;
        this.totalCount = totalCount;

        this.totalPage = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));

        this.currentPage = Math.max(1, Math.min(currentPage, totalPage));

        this.offset = (this.currentPage - 1) * pageSize;

        int pageBlockSize = 5;

        this.startPage = ((this.currentPage - 1) / pageBlockSize) * pageBlockSize + 1;

        this.endPage = Math.min(startPage + pageBlockSize - 1, totalPage);
    }

    public boolean isHasPrevious() {
        return currentPage > 1;
    }

    public boolean isHasNext() {
        return currentPage < totalPage;
    }
}