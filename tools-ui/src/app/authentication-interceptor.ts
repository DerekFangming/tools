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
                let logs = response.url;
                if(response instanceof HttpErrorResponse) {
                    logs = 1 + logs;
                    if (response.url.includes('authentication/login')) {
                        window.location.href = environment.urlPrefix + 'login-redirect?goto=' + window.location.href;
                        logs = 2 + logs;
                        console.log(logs);
                        return;
                    } else if (response.status == 0) {
                        window.location.href = environment.urlPrefix + 'login-redirect?goto=' + window.location.href;
                        logs = 9 + logs;
                        console.log(logs);
                        return;
                    }
                    logs = 3 + logs;
                }
                console.log(logs);
                console.log(response);
                console.log(response.status);
                return of(response);
            }));
	}
}