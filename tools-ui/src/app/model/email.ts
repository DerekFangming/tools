export class Email {
  id: number | undefined
  from: string | undefined
  to: string | undefined
  subject: string | undefined
  content: string | undefined

  senderType: string | undefined
  attachment: string | undefined
  html: boolean | undefined

  created: string | undefined
  read: boolean | undefined
  error: string | undefined
}