export function formatBrl(value: number | null | undefined): string {
  if (value == null) return '';
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
