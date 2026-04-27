export interface Beneficio {
  id?: number;
  nome: string;
  descricao?: string;
  valor: number | null;
  ativo?: boolean;
  version?: number;
}
