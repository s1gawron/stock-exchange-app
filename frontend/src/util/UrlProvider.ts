export default abstract class UrlProvider {
    protected _version: string = "";
    protected _path: string = "";

    abstract getHostSuffix(): string;

    public provide(): string {
        let url: string = import.meta.env.VITE_API_SERVICE_SCHEME + "://" + import.meta.env.VITE_API_SERVICE_HOST;
        const port: string | undefined = import.meta.env.VITE_API_SERVICE_PORT;

        if (port !== undefined) {
            url = url + ":" + port;
        }

        return url + "/" + this.getHostSuffix() + "/" + this._version + "/" + this._path;
    }
}