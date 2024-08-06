export const KEYS: string[] = [
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
];

type Keys = typeof KEYS[number];

export interface IndexCompanyDTO {
    ticker: string;
    name: string;
    industry: string;
}

export interface StockListingsDTO {
    count: number;
    indexCompanies: {
        [key in Keys]: IndexCompanyDTO[]
    }
}

export function generateInitialStockListingsState(): StockListingsDTO {
    const indexCompanies = {} as StockListingsDTO['indexCompanies'];
    KEYS.forEach(key => indexCompanies[key] = [])

    return {
        count: 0,
        indexCompanies: indexCompanies
    };
}
