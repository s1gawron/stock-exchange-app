export type ResponseDTO<T> =
    | { readonly success: true, readonly responseBody: T }
    | { readonly success: false, readonly errorMsg: string };

export function successResponse<T>(responseBody: T): ResponseDTO<T> {
    return {success: true, responseBody};
}

export function errorResponse<T>(errorMsg: string): ResponseDTO<T> {
    return {success: false, errorMsg};
}
