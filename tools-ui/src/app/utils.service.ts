import { Injectable } from "@angular/core"
import { environment } from "../environments/environment"
import { SpendingTransaction } from "./model/spending-transaction"

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  getCreatedTime(time: string) {
    return new Date(time).toLocaleString()
  }

  getType(input: string) {
    if (input == null) return ''

    return input
    .split("_")
    .reduce((res, word, i) =>
      `${res}${word.charAt(0).toUpperCase()}${word
        .substr(1)
        .toLowerCase()}`,
      ""
    )
  }

  logout() {
    window.location.href = environment.urlPrefix + 'logout'
  }

  processTransaction(transaction: SpendingTransaction, bank: string) {
    transaction = this.processTransactionByBank(transaction, bank)
    return this.processTransactionCategory(transaction)
  }

  processTransactionByBank(transaction: SpendingTransaction, bank: string) {
    let name = transaction.name.toLocaleLowerCase()
    if (bank == 'AMEX') {
      if (transaction.category == 'Merchandise & Supplies-Groceries') transaction.category = 'Grocery'
      if (transaction.location.includes('\n')) transaction.location = transaction.location.split('\n').join(', ')
      if (transaction.name.startsWith('H-E-B')) this.updateTransactionName(transaction, 'H-E-B')
    } else if (bank == 'Chase') {
      if (name.includes('costco')) transaction.category = 'Grocery'
      else if (name.includes('spotify') || name.includes('netflix') || name.includes('github') || name.includes('tesla') || name.includes('godaddy')) transaction.category = 'Subscription'
      else if (name.includes('dps')) transaction.category = 'Government'
      else if (name.includes('txtag')) transaction.category = 'Transportation'
      else if (name.includes('vzwrlss')) transaction.category = 'Utility'
      else if (name.includes('amazon')) transaction.category = 'Shopping'
      
    } else if (bank == 'BOA') {
      if (name.includes('uber')) transaction.category = 'Travel'
    }

    // Generic
    if (name.includes('&amp;')) this.updateTransactionName(transaction, transaction.name.replace(/&amp;/g, '&'))
    if (/(\+\d{1,2}\s)?\(?\d{3}\)?[\s.-]\d{3}[\s.-]\d{4}/.test(name)) this.updateTransactionName(transaction, transaction.name.replace(/(\+\d{1,2}\s)?\(?\d{3}\)?[\s.-]\d{3}[\s.-]\d{4}/g, ''))
    
      // Specific
    if (name.includes('costco')) this.updateTransactionName(transaction, 'Costco')
    else if (name.includes('cvs')) this.updateTransactionName(transaction, 'CVS')
    else if (name.includes('home') || name.includes('depot')) this.updateTransactionName(transaction, 'The Home Depot')
    else if (name.includes('aliexpress')) this.updateTransactionName(transaction, 'aliexpress')
    else if (name.includes('amazon')) this.updateTransactionName(transaction, 'Amazon')
    else if (name.includes('amzn mktp')) this.updateTransactionName(transaction, 'Amazon')
    else if (name.includes('prime video')) this.updateTransactionName(transaction, 'Prime Video')
    else if (name.includes('taobao')) this.updateTransactionName(transaction, 'Taobao')
    else if (name.includes('weee')) this.updateTransactionName(transaction, 'Weee')
    else if (name.includes('trader joe')) this.updateTransactionName(transaction, 'Trader Joe')
    else if (name.includes('rover')) this.updateTransactionName(transaction, 'Rover')
    else if (name.includes('lululemon')) this.updateTransactionName(transaction, 'LuluLemon')
    else if (name.includes('apple')) this.updateTransactionName(transaction, 'Apple')
    else if (name.includes('txtag')) this.updateTransactionName(transaction, 'TxTag')
    else if (name.includes('spectrum')) this.updateTransactionName(transaction, 'Spectrum')
    else if (name.includes('uber')) this.updateTransactionName(transaction, 'Uber')
    else if (name.includes('alipay')) this.updateTransactionName(transaction, 'Alipay')
    else if (name.includes('godaddy')) this.updateTransactionName(transaction, 'Godaddy') 
    else if (name.includes('bluebonnet')) this.updateTransactionName(transaction, 'Bluebonnet')
    else if (name.includes('mesa rim')) this.updateTransactionName(transaction, 'Mesa Rim')
    else if (name.includes('ebay')) this.updateTransactionName(transaction, 'eBay')
    else if (name.includes('paypal')) this.updateTransactionName(transaction, 'PayPal')
    else if (name.includes('h-e-b')) this.updateTransactionName(transaction, 'H-E-B')
    else if (name.includes('99 ranch')) this.updateTransactionName(transaction, '99 Ranch')
    else if (name.includes('h mart')) this.updateTransactionName(transaction, 'H-Mart')
    else if (name.includes('fedex')) this.updateTransactionName(transaction, 'Fedex')
    else if (name.includes('netflix')) this.updateTransactionName(transaction, 'Netflix')
    else if (name.includes('github')) this.updateTransactionName(transaction, 'GitHub')
    else if (name.includes('rei.com')) this.updateTransactionName(transaction, 'Rei')
    else if (name.includes('wholefds')) this.updateTransactionName(transaction, 'Whole Food')
    else if (name.includes('spotify')) this.updateTransactionName(transaction, 'Spotify')
    else if (name.includes('wal-mart')) this.updateTransactionName(transaction, 'Walmart')
    else if (name.includes('target')) this.updateTransactionName(transaction, 'Target')
    else if (name.includes('lowes')) this.updateTransactionName(transaction, 'Lowes')
    else if (name.includes('expedia')) this.updateTransactionName(transaction, 'Expedia') 
    else if (name.includes('starbucks')) this.updateTransactionName(transaction, 'Starbucks')
    else if (name.includes('united')) this.updateTransactionName(transaction, 'United')
    else if (name.includes('delta air')) this.updateTransactionName(transaction, 'Delta Air')
    else if (name.includes('bswhealth')) this.updateTransactionName(transaction, 'BswHealth')
    else if (name.includes('american air')) this.updateTransactionName(transaction, 'American Air')
    else if (name.includes('hertz')) this.updateTransactionName(transaction, 'Hertz')
    else if (name.includes('southwes')) this.updateTransactionName(transaction, 'Southwest Air')
    else if (name.includes('alaska air')) this.updateTransactionName(transaction, 'Alaska Air')
    else if (name.includes('petsmart')) this.updateTransactionName(transaction, 'Petsmart')
    else if (name.includes('texaco')) this.updateTransactionName(transaction, 'Texaco')
    else if (name.includes('austin-bergstrom')) this.updateTransactionName(transaction, 'Austin-Bergstrom')
    else if (name.includes('love\'s')) this.updateTransactionName(transaction, 'Love\'s')
    else if (name.includes('pilot_')) this.updateTransactionName(transaction, 'Pilot')
    else if (name.includes('flying j')) this.updateTransactionName(transaction, 'Flying J')
    else if (name.includes('chevron')) this.updateTransactionName(transaction, 'Chevron')
    else if (name.includes('exxon')) this.updateTransactionName(transaction, 'Exxon')
    else if (name.includes('buc-ee\'s')) this.updateTransactionName(transaction, 'Buc-ee\'s')
    else if (name.includes('7-eleven')) this.updateTransactionName(transaction, '7-Eleven')
    else if (name.includes('circle k')) this.updateTransactionName(transaction, 'Circle K')
    else if (name.includes('speedy stop')) this.updateTransactionName(transaction, 'Speedy Stop')
    else if (name.includes('usps po')) this.updateTransactionName(transaction, 'USPS')
    else if (name.includes('safeway')) this.updateTransactionName(transaction, 'Safeway')
    else if (name.includes('academy sports')) this.updateTransactionName(transaction, 'Academy Sports')
    else if (name.includes('walgreens')) this.updateTransactionName(transaction, 'Walgreens')
    else if (name.includes('shell oil')) this.updateTransactionName(transaction, 'Shell Oil')

      
      

    return transaction
  }

  updateTransactionName(transaction: SpendingTransaction, newName: string) {
    console.log('===========================================1')
    console.log(transaction)
    if (transaction.originalName == null) {
      console.log('===========================================11')
      transaction.originalName = transaction.name
    } else {
      console.log('===========================================12')
      console.log(transaction.originalName)
    }
    transaction.name = newName
    console.log('===========================================2')
    console.log(transaction)
  }

  processTransactionCategory(transaction: SpendingTransaction) {
    transaction.date = new Date(transaction.date).toISOString().split('T')[0]
    transaction.identifier = `${transaction.accountId}#${transaction.date}#${transaction.amount}`
    if (transaction.category == null){
      for (const [key, value] of nameToCategory.entries()) {
        if (transaction.name.toLocaleLowerCase().includes(key)) {
          transaction.category = value
          break
        }
      }
    } else {
      for (const [key, value] of categoryConvertion.entries()) {
        if (transaction.category == key) {
          transaction.category = value
          break
        }
      }
      if (!transactionCategories.includes(transaction.category)) {
        console.log(`${transaction.name} has category ${transaction.category}`)
        transaction.category = null
      }
    }

    if (transaction.category == null) transaction.category = 'Other'
    return transaction
  }
  
}

const nameToCategory = new Map([
  ['txtag', 'Transportation'],
  ['uber', 'Transportation'],
  ['76 - ', 'Transportation'],
  ['texaco', 'Transportation'],
  ['7-eleven', 'Transportation'],
  ['sheraton', 'Travel'],
  ['vehreg', 'Government'],
  ['tx.gov', 'Government'],
  ['tpwd', 'Government'],
  ['spectrum', 'Utility'],
  ['bluebonnet', 'Utility'],
  ['city of austin', 'Utility'],
  ['godaddy', 'Subscription'],
  ['apple', 'Subscription'],
  ['ring yearly plan', 'Subscription'],
  ['mesa rim', 'Subscription'],
  ['ownwell', 'Real Estate'],
  ['frozen custard', 'Restaurant'],
  ['kitchen', 'Restaurant'],
  ['fooda', 'Restaurant'],
  ['bakery', 'Restaurant'],
  ['chicken', 'Restaurant'],
  ['seatgeek', 'Entertainment'],
  ['electronic arts', 'Entertainment'],
  ['chanel', 'Shopping'],
  ['gucci', 'Shopping'],
  ['bakerty', 'Shopping'],
  ['alipay', 'Shopping'],
  ['aliexpress', 'Shopping'],
  ['home depot', 'Shopping'],
  ['homedepot', 'Shopping'],
  ['paypal', 'Shopping'],
  ['ebay', 'Shopping'],
  ['costco', 'Grocery'],
  ['pharmacy', 'Healthcare'],
  ['dermatology', 'Healthcare'],
  ['diagnostics', 'Healthcare'],
])

const categoryConvertion = new Map([
  ['Home', 'Shopping'],
  ['Personal', 'Shopping'],
  ['Health & Wellness', 'Healthcare'],
  ['Food & Drink', 'Restaurant'],
  ['Groceries', 'Grocery'],
  ['Gas', 'Transportation'],
  ['Automotive', 'Transportation'],
])

export const transactionCategories = ['Transportation', 'Government', 'Utility', 'Subscription', 'Real Estate',
  'Restaurant', 'Entertainment', 'Shopping', 'Grocery', 'Healthcare', 'Travel']
