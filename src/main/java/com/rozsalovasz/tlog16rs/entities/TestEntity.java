/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import com.avaje.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author precognox
 */
@Entity
@Getter
@Setter
public class TestEntity extends Model {

	private String textTest;

    @Id @GeneratedValue
	private int Id;

}
