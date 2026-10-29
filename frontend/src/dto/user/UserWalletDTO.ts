export interface UserWalletDTO {
    currency: string;
    stockValue: number;
    balanceAvailable: number;
    balanceBlocked: number;
    value: number;
    lastDayValue: number;
    valuePercentageChange: number;
    lastUpdateDate: string;
}
