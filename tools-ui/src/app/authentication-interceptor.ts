import { Injectable } from '@angular/core'
import { HttpInterceptor, HttpEvent, HttpRequest, HttpHandler, HttpErrorResponse } from '@angular/common/http'
import { Observable, of, throwError } from 'rxjs'
import { catchError } from 'rxjs/operators'
import { environment } from '../environments/environment'

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {
  constructor() {}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (request.url != 'https://api.imgur.com/3/image') {
      request = request.clone({
			  withCredentials: true
      });
    }

    return next.handle(request).pipe(
      catchError((response: HttpErrorResponse) => {
        if (response.url?.includes('/login')) {
          window.location.href = environment.urlPrefix + 'login-redirect?goto=' + window.location.href
          return of()
      } else if (response.status == 0) {
          window.location.href = environment.urlPrefix + 'login-redirect?goto=' + window.location.href
          return of()
        }

        return throwError(() => response.error)
      })
    )
  }
}
