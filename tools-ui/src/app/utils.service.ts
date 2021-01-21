import { Injectable } from "@angular/core";
import { environment } from "../environments/environment";

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

  logout() {
    window.location.href = environment.urlPrefix + 'logout';
  }
  
}

export function getBaseLocation() {
  let paths: string[] = location.pathname.split('/').splice(1, 1);
  let basePath: string = (paths && paths[0]) || '';
  return '/' + basePath;
}