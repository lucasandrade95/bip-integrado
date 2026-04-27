export type NotificationKind = 'success' | 'error';

export interface Notification {
  kind: NotificationKind;
  text: string;
}
