import axios from "axios";
import {StockListingsDTO} from "../../dto/stock/StockListingsDTO";
import StockListingsUrlProvider from "./StockListingsUrlProvider";
import ResponseDTO from "../../dto/user/ResponseDTO";

export async function getIndexStockListings(index: string | undefined): Promise<ResponseDTO<StockListingsDTO | null>> {
    if (index === undefined) {
        console.error("Index parameter is undefined!");
        return new ResponseDTO(false, null, "Cannot load stock listings. Reason: index not provided!");
    }

    const indexStockListingsUri: string = StockListingsUrlProvider.v2().index(index).provide();

    try {
        const res = await axios.get(indexStockListingsUri);
        return new ResponseDTO(true, res.data)
    } catch (err) {
        const errMsg = err instanceof Error ? err.message : "Unknown error";
        return new ResponseDTO(false, null, `Cannot load stock listings. Reason: ${errMsg}`);
    }
}
