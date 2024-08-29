export default abstract class UrlProvider {
    protected _version: string = "";
    protected _path: string = "";

    public provide(): string {
        let url: string = process.env.REACT_APP_API_SERVICE_SCHEME + "://" + process.env.REACT_APP_API_SERVICE_HOST;
        const port: string | undefined = process.env.REACT_APP_API_SERVICE_PORT;

        if (port !== undefined) {
            url = url + ":" + port;
        }

        return url + "/" + this._version + "/" + this._path;
    }
}