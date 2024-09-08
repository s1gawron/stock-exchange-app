import React from "react";
import axios from "axios";
import {StockListingsDTO} from "../../dto/stock/StockListingsDTO";
import StockListingsUrlProvider from "./StockListingsUrlProvider";

export function getIndexStockListings(index: string | undefined, setStockListings: React.Dispatch<React.SetStateAction<StockListingsDTO>>): void {
    if (index === undefined) {
        console.error("Index parameter is undefined!");
        return;
    }

    const indexStockListingsUri: string = StockListingsUrlProvider.v2().index(index).provide();

    axios.get(indexStockListingsUri)
        .then((res) => {
            setStockListings(res.data);
        })
        .catch((err) => console.log(err));
}
