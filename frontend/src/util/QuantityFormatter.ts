const QUANTITY_DECIMAL_PLACES: number = 6;

export function formatQuantity(quantity: number): string {
    return Number(quantity.toFixed(QUANTITY_DECIMAL_PLACES)).toString();
}
