import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  getCreatedTime(time: string) {
    return new Date(time).toLocaleString();
  }

  getType(input: string) {
    if (input == null) return '';

    return input
    .split("_")
    .reduce((res, word, i) =>
      `${res}${word.charAt(0).toUpperCase()}${word
        .substr(1)
        .toLowerCase()}`,
      ""
    );
  }
  
}