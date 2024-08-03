import React from "react";
import axios from "axios";
import {StockListingsDTO} from "../dto/stock/StockListingsDTO";

const BASE_URL: string = "http://localhost:8080/api/public/stock";
const CURRENT_API_VERSION: string = "/v2"
const STOCK_LISTINGS_ENDPOINT: string = "/index";

export function getIndexStockListings(index: string | undefined, queryParam: string | null, setStockListings: React.Dispatch<React.SetStateAction<StockListingsDTO>>): void {
    if (index === undefined) {
        console.error("Index parameter is undefined!");
        return;
    }

    const query: string = queryParam === null ? 'A' : queryParam;

    const indexStockListingsUri: string = BASE_URL + CURRENT_API_VERSION + STOCK_LISTINGS_ENDPOINT + "/" + index;

    axios.get(indexStockListingsUri)
        .then((res) => {
            console.log(res.data);
            setStockListings(res.data[query]);
        })
        .catch((err) => console.log(err));
}
