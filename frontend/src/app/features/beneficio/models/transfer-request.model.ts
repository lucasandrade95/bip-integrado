export interface TransferRequest {
  fromId?: number | null;
  fromName?: string | null;
  toId?: number | null;
  toName?: string | null;
  amount: number;
}
