export class Post {
    id: number;
    title: string;
    imageNames: string[];

	constructor(id: number, title: string, imageNames: string[]) {
        this.id = id;
        this.title = title;
        this.imageNames = imageNames;
	}
}