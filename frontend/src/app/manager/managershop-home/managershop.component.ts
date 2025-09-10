import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { ReactiveFormsModule } from '@angular/forms'; 
import { FooterComponent } from '../../layout/footer/footer.component';
import { ManagerSidebarComponent } from '../layout/manager-sidebar/manager-sidebar.component'; 

@Component({
  selector: 'app-managershop-home',
  standalone: true,
  imports: [
    CommonModule,          
    FormsModule,           
    ReactiveFormsModule,   
    ManagerSidebarComponent, 
    FooterComponent          
  ],
  templateUrl: './managershop.component.html',
  styleUrls: ['./managershop.component.scss']
})
export class ManagershopComponent { }