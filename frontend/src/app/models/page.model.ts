export interface Sort {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
}
export interface Pageable {
    pageNumber: number;
    pageSize: number;
    sort: Sort;
    offset: number;
    paged: boolean;
    unpaged: boolean;
}
export interface Page<T> {
    content: T[];
    pageable: Pageable;
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // Trang hiện tại (0-based)
    sort: Sort;
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}
export interface Sort { 
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
}
