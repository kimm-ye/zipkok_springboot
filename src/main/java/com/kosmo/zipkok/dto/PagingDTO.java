package com.kosmo.zipkok.util;

public class Paging {
    private final int page;         // 1-based
    private final int size;         // page size
    private final int totalCount;   // 총 레코드 수
    private final int totalPages;   // 총 페이지 수
    private final int offset;       // LIMIT..OFFSET
    private final int limit;        // LIMIT
    private final int blockSize;    // 페이징 블록 크기 (ex. 10)
    private final int startPage;    // 현재 블록 시작 페이지
    private final int endPage;      // 현재 블록 끝 페이지
    private final boolean hasPrev;  // 이전 블록 존재
    private final boolean hasNext;  // 다음 블록 존재

    private Paging(int page, int size, int totalCount, int blockSize) {
        this.size = Math.max(1, Math.min(size, 100));
        this.totalCount = Math.max(0, totalCount);
        this.totalPages = Math.max(1, (int)Math.ceil((double)this.totalCount / this.size));

        this.page = Math.max(1, Math.min(page, this.totalPages));
        this.offset = (this.page - 1) * this.size;
        this.limit = this.size;

        this.blockSize = blockSize > 0 ? blockSize : 10;
        int currentBlock = (this.page - 1) / this.blockSize;
        this.startPage = currentBlock * this.blockSize + 1;
        this.endPage = Math.min(this.startPage + this.blockSize - 1, this.totalPages);
        this.hasPrev = this.startPage > 1;
        this.hasNext = this.endPage < this.totalPages;
    }

    public static Paging of(int page, int size, int totalCount) {
        return new Paging(page, size, totalCount, 10);
    }
    public static Paging of(int page, int size, int totalCount, int blockSize) {
        return new Paging(page, size, totalCount, blockSize);
    }

    // getters
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalCount() { return totalCount; }
    public int getTotalPages() { return totalPages; }
    public int getOffset() { return offset; }
    public int getLimit() { return limit; }
    public int getBlockSize() { return blockSize; }
    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }
    public boolean isHasPrev() { return hasPrev; }
    public boolean isHasNext() { return hasNext; }
}
