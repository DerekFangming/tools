export class Email {
  id: number;
  from: string;
  to: string;
  subject: string;
  content: string;

  senderType: string;
  attachment: string;
  html: boolean;

  created: string;
  read: boolean;
  error: string;
}