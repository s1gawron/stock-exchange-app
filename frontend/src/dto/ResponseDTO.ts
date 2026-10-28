export default class ResponseDTO<T> {
    private readonly _responseBody?: T;
    private readonly _success: boolean;
    private readonly _errorMsg?: string;

    public constructor(success: boolean, responseBody?: T, errorMsg?: string) {
        this._success = success;
        this._responseBody = responseBody;
        this._errorMsg = errorMsg;
    }


    get responseBody(): T | undefined {
        return this._responseBody;
    }

    get success(): boolean {
        return this._success;
    }

    get errorMsg(): string | undefined {
        return this._errorMsg;
    }
}