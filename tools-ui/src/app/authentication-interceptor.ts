import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpEvent, HttpRequest, HttpHandler, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {
    constructor() {}
    
	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		request = request.clone({
			withCredentials: true
        });
        return next.handle(request).pipe(
            tap(() => {
            }),
            catchError((response: any) => {
                console.log('AAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
                console.log(response.url);
                if(response instanceof HttpErrorResponse) {
                    if (response.url.includes('authentication/login')) {
                        window.location.href = environment.urlPrefix + 'login-redirect?goto=' + window.location.href;
                        return;
                    }
                }
                return of(response);
            }));
	}
}